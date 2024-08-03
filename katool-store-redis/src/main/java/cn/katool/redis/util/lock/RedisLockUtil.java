/**
 * Title
 *
 * @ClassName: RedisLockUtil
 * @Description:锁工具类,通过内部枚举类实现单例，防止反射攻击
 * @author: Karos
 * @date: 2023/1/4 0:17
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.redis.util.lock;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.redis.config.util.LockConfig;
import cn.katool.util.ScheduledTaskUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

import static cn.katool.redis.util.lock.LockMessageWatchDog.LOCK_MQ_NAME;
import static cn.katool.redis.util.lock.LockMessageWatchDog.threadWaitQueue;

@Slf4j
public class RedisLockUtil {
        @Resource
        RedisTemplate redisTemplate;
        // lockName:uID:numbers
        // key[1] 锁名
        // ARGV[1] UID
        // ARGV[2] 时间
        @Value("if redis.call('exists',KEYS[1]) ~= 0 then\n" +        // 这个锁是否存在
                "        if redis.call('hexists',KEYS[1],ARGV[1] ) == 0 then\n" +       // 存在但不是自己的锁
                "            return redis.call('pttl',KEYS[1]);\n" +                  // 返回剩余时间
                "        end\n" +
//                "        -- 如果是自己的锁就记录次数\n" +
                "        redis.call('hincrby',KEYS[1],ARGV[1],1);\n" +                // 是自己的锁重入
                "else\n" +
                "        redis.call('hset',KEYS[1],ARGV[1],1);\n" +  // 没有的话就加上
                "end\n" +
                "redis.call('pexpire',KEYS[1],ARGV[2]);\n" +      //延期
                "return nil")             //返回
        private String lockScript;
        @Value("    if redis.call('exists',KEYS[1]) ~= 0 then\n" +                            //如果锁存在
                "        if redis.call('hexists',KEYS[1],ARGV[1]) ~= 0 then\n" +                      // 如果是自己的锁
                "                local inc= redis.call('hincrby',KEYS[1],ARGV[1],-1);\n" +
                "               if inc == 0 then" +
                "                    redis.call('hdel',KEYS[1],ARGV[1]);\n" +
                "               end" +
                "         return inc;\n" +
                "        end\n" +
                "    end\n" +
                " return nil\n")
        private String unLockScript;
        @Value("    if redis.call('exists',KEYS[1]) ~= 0 then\n" +
                "       local lessTime = redis.call('pttl',KEYS[1]);\n" +
                "       redis.call('pexpire',KEYS[1],ARGV[1]);\n" +                            //如果锁存在
                "    end\n" +
                "    return redis.call('pttl',KEYS[1]);" )
        private String delayLockScript;
        public String serviceUUid=RandomUtil.randomString(16);
        private volatile static boolean isOpenCorn=false;
        /**
         * 带看门狗机制上锁
         * @param lockObj
         * @return
         */
        public boolean DistributedLock(Object lockObj,Boolean isAgress){
                try {
                        return DistributedLock(lockObj,null,null,true,false);
                } catch (KaToolException e) {
                        throw new RuntimeException(e);
                }
        }
        @Resource
        LockConfig lockConfig;
        //加锁
        public Long luaToRedisByLock(String lockName,Long expTime,TimeUnit timeUnit,String[] hashkey){
                long id = Thread.currentThread().getId();
                //生成hashkey
                String hashKey=hashkey[0]= DigestUtils.md5DigestAsHex(new String(id+serviceUUid).getBytes());
                log.debug("serviceUUid:{},id:{},hashKey:{}",serviceUUid,id,hashKey);
                DefaultRedisScript defaultRedisScript = new DefaultRedisScript();
                ArrayList<String> keys = new ArrayList<>();
                ArrayList<Object> args = new ArrayList();
                keys.add(lockName);
                args.add(hashKey);
                args.add(TimeoutUtils.toMillis(expTime,timeUnit));
                defaultRedisScript.setScriptText(lockScript);
                defaultRedisScript.setResultType(Long.class);
//                log.debug("lockLua -> {}",lockScript);
                Long execute = (Long) redisTemplate.execute(defaultRedisScript, keys, args.toArray());
                if (ObjectUtil.isEmpty(execute)) {
                        log.debug("【KaTool::RedisLockUtil】 {}成功抢到锁，锁名：{}，过期时间：{}，单位：{}",hashKey,lockName,expTime,timeUnit);
                }
                return execute;
        }
        //释放锁
        @Transactional
        public Long luaToRedisByUnLock(String lockName,Thread thread){
                long id = thread==null?Thread.currentThread().getId():thread.getId();
                //生成value1
                String hashKey= DigestUtils.md5DigestAsHex(new String(id+serviceUUid).getBytes());
                DefaultRedisScript defaultRedisScript = new DefaultRedisScript();
                ArrayList<String> keys = new ArrayList<>();
                ArrayList<String> args = new ArrayList();
                keys.add(lockName);
                args.add(hashKey);
                defaultRedisScript.setScriptText(unLockScript);
                defaultRedisScript.setResultType(Long.class);
//                log.debug("unlockLua -> {}",unLockScript);
                Long remainLocks = (Long) redisTemplate.execute(defaultRedisScript, keys, args.toArray());
                if (ObjectUtil.isEmpty(remainLocks)) {
                        log.error("【KaTool::RedisLockUtil】 {}释放锁失败，请释放自己的锁，锁名：{}",hashKey,lockName);
                }
                else if(remainLocks==0){
                        log.debug("【KaTool::RedisLockUtil】 {}成功释放锁，锁名：{}",hashKey,lockName);
                        redisTemplate.convertAndSend(LOCK_MQ_NAME,lockName);
                }
                return remainLocks;
        }
        public Boolean luaToRedisByDelay(String lockName,Long expTimeInc,TimeUnit timeUnit){
                long id = Thread.currentThread().getId();
                DefaultRedisScript defaultRedisScript = new DefaultRedisScript();
                ArrayList<String> keys = new ArrayList<>();
                ArrayList<Object> args = new ArrayList();
                keys.add(lockName);
                args.add(TimeoutUtils.toMillis(expTimeInc,timeUnit)+3000);
                defaultRedisScript.setScriptText(delayLockScript);
                defaultRedisScript.setResultType(Long.class);
                Long expire = redisTemplate.getExpire(lockName);
                Long execute = (Long) redisTemplate.execute(defaultRedisScript, keys, args.toArray());
                return execute>expire;
        }
        /**
         * @param obj
         * @param exptime
         * @param timeUnit
         * @return
         * @throws KaToolException
         */
        public boolean DistributedLock(Object obj,Long exptime,TimeUnit timeUnit,Boolean isDelay,Boolean isAgress) throws KaToolException {
                if (ObjectUtil.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock =>  传入obj为空");
                }
                if(ObjectUtil.isEmpty(exptime)){
                        exptime= lockConfig.getInternalLockLeaseTime();;
                }
                if (ObjectUtils.isEmpty(timeUnit)){
                        timeUnit=lockConfig.getTimeUnit();
                }
                String lockName="Lock:"+obj.toString();
                Long aLong = -1L;
                // 进入互斥等待
                Thread currentThread = Thread.currentThread();
                ConcurrentLinkedQueue<Thread> threads = threadWaitQueue.get(lockName);
                while(true){
                        String[] hashkey = new String[1];
                                aLong=luaToRedisByLock(lockName, exptime, timeUnit, hashkey);
                                if (aLong==null) {
                                        break;
                                }
                        log.debug("【KaTool::RedisLockUtil】 {}未抢到锁，线程等待通知唤醒，最多等待时间：{}，锁名：{}，过期时间：{}，单位：{}",hashkey[0],aLong,lockName,exptime,timeUnit);
//                                Thread.sleep(aLong/3);     // 初步改进：使用线程休眠，采用自旋锁+线程互斥
                        if (ObjectUtil.isEmpty(threads)){
                                synchronized (lockName.intern()){
                                        threads=threadWaitQueue.get(lockName);
                                        if (ObjectUtil.isEmpty(threads)){
                                                threads=new ConcurrentLinkedQueue<Thread>();
                                log.debug("【{}-created】新增线程进入lock:{}等待队列",Thread.currentThread().getId(),lockName);
                                threadWaitQueue.putIfAbsent(lockName,threads);
                                log.debug("【{}-created】threadWaitQueue:{},threads:{}",Thread.currentThread().getId(),threadWaitQueue,threads);
                                        }
                                }
                        }
                        if (!threads.contains(currentThread)) {
                                log.debug("【{}-add】新增线程进入lock:{}等待队列",Thread.currentThread().getId(),lockName);
                                threads.add(currentThread);
                                log.debug("【{}-add】threadWaitQueue:{},threads:{}",Thread.currentThread().getId(),threadWaitQueue,threads);
                        }
                        log.debug("threadWaitQueue:{}",threadWaitQueue);;
                        if(isAgress){
                                LockSupport.parkNanos(((aLong<<1)+aLong)>>1);             // 自行争取一次
                        }
                        else{
                                LockSupport.park();
                        }
                        log.debug("katool=> {}未抢到锁，线程被唤醒，重新抢锁，锁名：{}，过期时间：{}，单位：{}",hashkey[0],lockName,exptime,timeUnit);
                }
                // 获得线程锁，被唤醒后删除自身的thread队列，避免死锁
                if (threads!=null&&threads.contains(currentThread)) {
                        threads.remove(currentThread);
                }
                //实现看门狗
                if (isDelay){
                        Thread thread = currentThread;
                        TimeUnit finalTimeUnit = timeUnit;
                        Long finalExptime = exptime;
                        ScheduledFuture<?> scheduledFuture = ScheduledTaskUtil.submitTask(new Runnable() {
                                @SneakyThrows
                                @Override
                                public void run() {
                                        boolean alive = thread.isAlive()||thread.isInterrupted();
                                        ScheduledFuture future = threadWatchDog.get(thread.getId());
                                        if (alive) {
                                                log.debug("Thread ID:{} 线程仍然存活，看门狗续期中...", thread.getId());
                                                delayDistributedLock(obj, finalExptime, finalTimeUnit);
                                                return;
                                        } else {
                                                if (future.isCancelled()||future.isDone()) {
                                                        log.error("Thread ID:{} 线程已经死亡，但是没有对应的scheduleId", thread.getId());
                                                        return;
                                                }
                                                log.debug("Thread ID:{} 线程死亡，看门狗自动解锁", thread.getId());
                                              DistributedUnLock(obj,thread);
                                                future.cancel(true);
                                                return;
                                        }
                                }
                        },finalExptime >= 3 ? (finalExptime) / 3 : finalExptime, finalExptime >= 3 ? (finalExptime) / 3 : finalExptime, finalTimeUnit);
                        threadWatchDog.put(thread.getId(),scheduledFuture);
                }
                return BooleanUtil.isTrue(aLong!=null);
        }
        private static ConcurrentHashMap<Long,ScheduledFuture> threadWatchDog=new ConcurrentHashMap<>();
        public static ConcurrentHashMap<Long, ScheduledFuture> getThreadWatchDog() {
                return threadWatchDog;
        }
        //延期
        public boolean delayDistributedLock(Object obj,Long exptime,TimeUnit timeUnit) throws KaToolException {
                if (ObjectUtils.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock =>  传入obj为空");
                }
                //本身就是一句话，具备原子性，没有必要使用lua脚本
                Boolean expire = luaToRedisByDelay("Lock:"+obj.toString(),exptime,timeUnit);
                log.debug("【KaTool::LockUntil】  =>  delayDistributedLock:{} value:{} extime:{} timeUnit:{}",obj.toString(), "1", exptime, timeUnit);
                return BooleanUtil.isTrue(expire);
        }
        //释放锁
        public Long DistributedUnLock(Object obj) throws KaToolException {
                Long remainLocks = DistributedUnLock(obj, null);
                return remainLocks;
        }
        public Long DistributedUnLock(Object obj,Thread thread) throws KaToolException {
                if (ObjectUtils.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock =>  传入obj为空");
                }
//                由于这里有了可重入锁，不应该直接删除Boolean aBoolean = redisTemplate.delete("Lock:" + obj.toString());
                Long remainLocks = luaToRedisByUnLock("Lock:" + obj.toString(),thread);
                if (null != remainLocks && remainLocks == 0){
                        threadWatchDog.get(Thread.currentThread().getId()).cancel(true);
                        threadWatchDog.remove(Thread.currentThread().getId());
                }
                log.debug("【KaTool::LockUntil】  =>  unDistributedLock:{} isdelete:{} watchDog is cancel and drop",obj.toString(),true);
                return remainLocks;
        }
        //利用枚举类实现单例模式，枚举类属性为静态的
        private static enum SingletonFactory{
                Singleton;
                RedisLockUtil redisLockUtil;
                private SingletonFactory(){
                        redisLockUtil =new RedisLockUtil();
                }
                public RedisLockUtil getInstance(){
                        return redisLockUtil;
                }
        }
        public static RedisLockUtil getInstance(){
                return SingletonFactory.Singleton.redisLockUtil;
        }
        private RedisLockUtil(){
        }
}
