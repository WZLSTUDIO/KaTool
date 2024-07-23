package cn.katool.util.lock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
public class LocalLockMap {
    private static ConcurrentHashMap<String, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();
    public static ReentrantReadWriteLock getLock(String key){
        return lockMap.getOrDefault(key,new ReentrantReadWriteLock(true));
    }
    public static ReadLock getReadLock(String key){
        return getLock(key).readLock();
    }
    public static WriteLock getWriteLock(String key){
        return getLock(key).writeLock();
    }
}
