package org.example;

import javassist.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class PDFCrack {

    public static void main(String[] args) throws Exception {
        // 这个是jar包的存放路径
        String jarPath = "lib/aspose-pdf-24.12.jar";
        // 破解方法
//        crack1(jarPath);//不太灵
        crack2(jarPath);//可行
    }

    private static void crack2(String jarName) {
        try {
            ClassPool.getDefault().insertClassPath(jarName);
            CtClass ctClass = ClassPool.getDefault().getCtClass("com.aspose.pdf.ADocument");
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
            int num = 0;
            for (int i = 0; i < declaredMethods.length; i++) {
                if (num == 2) {
                    break;
                }
                CtMethod method = declaredMethods[i];
                CtClass[] ps = method.getParameterTypes();
                if (ps.length == 2
                        && method.getName().equals("lI")
                        && ps[0].getName().equals("com.aspose.pdf.ADocument")
                        && ps[1].getName().equals("int")) {
                    // 最多只能转换4页 处理
                    System.out.println(method.getReturnType());
                    System.out.println(ps[1].getName());
                    method.setBody("{return false;}");
                    num = 1;
                }
                if (ps.length == 0 && method.getName().equals("lt")) {
                    // 水印处理
                    method.setBody("{return true;}");
                    num = 2;
                }
            }
            File file = new File(jarName);
            ctClass.writeFile(file.getParent());
            disposeJar(jarName, file.getParent() + "/com/aspose/pdf/ADocument.class");
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void disposeJar(String jarName, String replaceFile) {
        List<String> deletes = new ArrayList<>();
        // 这个是22.* 版本的
        deletes.add("META-INF/37E3C32D.SF");
        deletes.add("META-INF/37E3C32D.RSA");

        // 这个是24.* 版本 使用这个
        deletes.add("META-INF/7DD91000.SF");
        deletes.add("META-INF/7DD91000.RSA");

        File oriFile = new File(jarName);
        if (!oriFile.exists()) {
            System.out.println("######Not Find File:" + jarName);
            return;
        }
        //将文件名命名成备份文件
        String bakJarName = jarName.substring(0, jarName.length() - 3) + "cracked.jar";
        //   File bakFile=new File(bakJarName);
        try {
            //创建文件（根据备份文件并删除部分）
            JarFile jarFile = new JarFile(jarName);
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(bakJarName));
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (!deletes.contains(entry.getName())) {
                    if (entry.getName().equals("com/aspose/pdf/ADocument.class")) {
                        System.out.println("Replace:-------" + entry.getName());
                        JarEntry jarEntry = new JarEntry(entry.getName());
                        jos.putNextEntry(jarEntry);
                        FileInputStream fin = new FileInputStream(replaceFile);
                        byte[] bytes = readStream(fin);
                        jos.write(bytes, 0, bytes.length);
                    } else {
                        jos.putNextEntry(entry);
                        byte[] bytes = readStream(jarFile.getInputStream(entry));
                        jos.write(bytes, 0, bytes.length);
                    }
                } else {
                    System.out.println("Delete:-------" + entry.getName());
                }
            }
            jos.flush();
            jos.close();
            jarFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }
    /**
     * 好像不太灵
     * @param jarName
     */
    private static void crack1(String jarName) {
        try {
            ClassPool.getDefault().insertClassPath(jarName);
            CtClass ctClass = ClassPool.getDefault().getCtClass("com.aspose.pdf.ADocument");
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
            int num = 0;
            for (int i = 0; i < declaredMethods.length; i++) {
                if (num == 2) {
                    break;
                }
                CtMethod method = declaredMethods[i];
                CtClass[] ps = method.getParameterTypes();
                if (ps.length == 2
                        && method.getName().equals("lI")
                        && ps[0].getName().equals("com.aspose.pdf.ADocument")
                        && ps[1].getName().equals("int")) {
                    // 最多只能转换4页 处理
                    System.out.println(method.getReturnType());
                    System.out.println(ps[1].getName());
                    method.setBody("{return false;}");
                    num = 1;
                }
                if (ps.length == 0 && method.getName().equals("lt")) {
                    // 水印处理
                    method.setBody("{return true;}");
                    num = 2;
                }
            }
            File file = new File(jarName);
            ctClass.writeFile(file.getParent());
            disposeJar(jarName, file.getParent() + "/com/aspose/pdf/ADocument.class");
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    private static void disposeJar(String jarName, String replaceFile) {
//        List<String> deletes = new ArrayList<>();
//        deletes.add("META-INF/7DD91000.SF");
//        deletes.add("META-INF/7DD91000.RSA");
//        File oriFile = new File(jarName);
//        if (!oriFile.exists()) {
//            System.out.println("######Not Find File:" + jarName);
//            return;
//        }
//        //将文件名命名成备份文件
//        String bakJarName = jarName.substring(0, jarName.length() - 3) + "cracked.jar";
//        //   File bakFile=new File(bakJarName);
//        try {
//            //创建文件（根据备份文件并删除部分）
//            JarFile jarFile = new JarFile(jarName);
//            JarOutputStream jos = new JarOutputStream(new FileOutputStream(bakJarName));
//            Enumeration entries = jarFile.entries();
//            while (entries.hasMoreElements()) {
//                JarEntry entry = (JarEntry) entries.nextElement();
//                if (!deletes.contains(entry.getName())) {
//                    if (entry.getName().equals("com/aspose/pdf/ADocument.class")) {
//                        System.out.println("Replace:-------" + entry.getName());
//                        JarEntry jarEntry = new JarEntry(entry.getName());
//                        jos.putNextEntry(jarEntry);
//                        FileInputStream fin = new FileInputStream(replaceFile);
//                        byte[] bytes = readStream(fin);
//                        jos.write(bytes, 0, bytes.length);
//                    } else {
//                        jos.putNextEntry(entry);
//                        byte[] bytes = readStream(jarFile.getInputStream(entry));
//                        jos.write(bytes, 0, bytes.length);
//                    }
//                } else {
//                    System.out.println("Delete:-------" + entry.getName());
//                }
//            }
//            jos.flush();
//            jos.close();
//            jarFile.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static byte[] readStream(InputStream inStream) throws Exception {
//        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int len = -1;
//        while ((len = inStream.read(buffer)) != -1) {
//            outSteam.write(buffer, 0, len);
//        }
//        outSteam.close();
//        inStream.close();
//        return outSteam.toByteArray();
//    }

}