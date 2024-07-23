package cn.katool.util.classes;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
public class KaToolClassLoader extends ClassLoader{
    private String classPath;
    //    F:/demo1/
    //    完整路径为：F:/demo1/server/MyServlet.class
    public KaToolClassLoader(String classPath){
        this.classPath = classPath;
    }
    public KaToolClassLoader(String classPath, ClassLoader privateLoader){
        super(privateLoader);
        this.classPath = classPath;
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes;
        Class<?> clazz;
        try{
            bytes = getClassByte(name);
            //继承ClassLoader是为了用defineClass方法。
            clazz = defineClass(name, bytes, 0, bytes.length);
            return clazz;
        }catch (Exception e){
        }
        return super.findClass(name);
    }
    private byte[] getClassByte(String name){
        name = name.replaceAll("\\.","/");
        String realPath = this.classPath+name+".class";
        File f = new File(realPath);
        if(f.exists()){
            byte[] bytes = new byte[0];
            try {
                FileInputStream fis = new FileInputStream(f);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[1024];
                while ((len = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                bytes = baos.toByteArray();
                fis.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("从本地读取【"+realPath+"】完成");
            return  bytes;
        }else{
            return null;
        }
    }
}