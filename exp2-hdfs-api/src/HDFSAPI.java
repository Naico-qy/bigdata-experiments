import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HDFSAPI {

    private static final String HDFS_URI = "hdfs://localhost:9000";
    private static FileSystem fs;

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", HDFS_URI);
        System.setProperty("HADOOP_USER_NAME", "hadoop");

        fs = FileSystem.get(URI.create(HDFS_URI), conf);

        String base = "/user/hadoop/exp2_api";
        String localTxt = "/home/hadoop/experiment1/hdfs/local.txt";
        String addTxt   = "/home/hadoop/experiment1/hdfs/add.txt";

        // 0. 清理上次运行残留
        if (fs.exists(new Path(base))) {
            fs.delete(new Path(base), true);
        }
        fs.mkdirs(new Path(base));

        task1_upload(localTxt, base + "/local.txt", addTxt);
        task2_download(base + "/local.txt", "/home/hadoop/experiment1/hdfs/");
        task3_cat(base + "/local.txt");
        task4_metadata(base + "/local.txt");
        task5_recursiveList(base);
        task6_createDeleteFile(base + "/newdir/newfile.txt");
        task7_createDeleteDir(base);
        task8_append(base + "/local.txt", addTxt);
        task9_deleteFile(base + "/local.txt");
        task10_move(base + "/moved/local.txt", base + "/renamed.txt", localTxt, base);

        fs.close();
        System.out.println("\n===== 全部任务执行完毕 =====");
    }

    /* ============ 1. 上传文件 ============ */
    static void task1_upload(String local, String remote, String appendSrc) throws Exception {
        System.out.println("\n===== 1. 上传文件（存在则追加或覆盖）=====");
        Path remotePath = new Path(remote);
        Path localPath = new Path(local);

        if (fs.exists(remotePath)) {
            System.out.println("文件已存在，先覆盖再追加...");
            fs.copyFromLocalFile(false, true, localPath, remotePath);
            System.out.println("已覆盖上传: " + remote);

            FSDataOutputStream out = fs.append(remotePath);
            BufferedReader br = new BufferedReader(new FileReader(appendSrc));
            String line;
            while ((line = br.readLine()) != null) {
                out.write((line + "\n").getBytes());
            }
            br.close();
            out.close();
            System.out.println("已追加: " + appendSrc);
        } else {
            fs.copyFromLocalFile(false, false, localPath, remotePath);
            System.out.println("首次上传成功: " + remote);
        }
    }

    /* ============ 2. 下载文件 ============ */
    static void task2_download(String remote, String localDir) throws Exception {
        System.out.println("\n===== 2. 下载文件（同名自动重命名）=====");
        Path remotePath = new Path(remote);
        String fileName = remotePath.getName();
        Path localPath = new Path(localDir + fileName);

        if (new File(localPath.toString()).exists()) {
            String newName = fileName + "_" + System.currentTimeMillis();
            localPath = new Path(localDir + newName);
            System.out.println("本地已存在同名文件，重命名为: " + newName);
        }
        fs.copyToLocalFile(false, remotePath, localPath, true);
        System.out.println("下载完成: " + localPath);
    }

    /* ============ 3. 输出内容 ============ */
    static void task3_cat(String remote) throws Exception {
        System.out.println("\n===== 3. 输出文件内容到终端 =====");
        FSDataInputStream in = fs.open(new Path(remote));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }

    /* ============ 4. 显示文件元信息 ============ */
    static void task4_metadata(String remote) throws Exception {
        System.out.println("\n===== 4. 文件元信息 =====");
        FileStatus status = fs.getFileStatus(new Path(remote));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("路径    : " + status.getPath());
        System.out.println("权限    : " + status.getPermission());
        System.out.println("大小    : " + status.getLen() + " 字节");
        System.out.println("副本数  : " + status.getReplication());
        System.out.println("属主    : " + status.getOwner());
        System.out.println("属组    : " + status.getGroup());
        System.out.println("修改时间: " + sdf.format(new Date(status.getModificationTime())));
    }

    /* ============ 5. 递归输出目录信息 ============ */
    static void task5_recursiveList(String dir) throws Exception {
        System.out.println("\n===== 5. 递归输出目录信息 =====");
        listRecursive(new Path(dir), 0);
    }

    static void listRecursive(Path path, int depth) throws Exception {
        FileStatus[] list = fs.listStatus(path);
        for (FileStatus s : list) {
            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < depth; i++) indent.append("  ");
            System.out.printf("%s%s\t%s\t%d字节\t%s%n",
                    indent, s.isDirectory() ? "[D]" : "[F]",
                    s.getPermission(), s.getLen(), s.getPath());
            if (s.isDirectory()) {
                listRecursive(s.getPath(), depth + 1);
            }
        }
    }

    /* ============ 6. 创建/删除文件（父目录自动创建）============ */
    static void task6_createDeleteFile(String remote) throws Exception {
        System.out.println("\n===== 6. 创建/删除文件（父目录不存在则自动创建）=====");
        Path filePath = new Path(remote);
        Path parent = filePath.getParent();

        if (!fs.exists(parent)) {
            fs.mkdirs(parent);
            System.out.println("已自动创建父目录: " + parent);
        }
        if (!fs.exists(filePath)) {
            fs.create(filePath).close();
            System.out.println("已创建文件: " + filePath);
        }
        fs.delete(filePath, false);
        System.out.println("已删除文件: " + filePath);
    }

	/* ============ 7. 创建/删除目录（空目录才删除）============ */
	static void task7_createDeleteDir(String base) throws Exception {
    	System.out.println("\n===== 7. 创建/删除目录（空目录才删除）=====");
    	Path d1 = new Path(base + "/d1/d2");
    	fs.mkdirs(d1);
    	System.out.println("已创建目录: " + d1);

    	// (1) 删除空目录：非递归 delete 可以成功
    	boolean ok = fs.delete(d1, false);
    	System.out.println("删除空目录 " + d1 + " : " + ok);

    	// (2) 演示非空目录不能删除：用 try-catch 捕获异常
    	Path d3 = new Path(base + "/d1/d3");
    	fs.mkdirs(d3);
    	fs.create(new Path(d3 + "/f.txt")).close();
    	System.out.println("已创建非空目录: " + d3 + "（内含 f.txt）");

    	try {
        	boolean ok2 = fs.delete(d3, false);   // 非递归，应抛异常
        	System.out.println("非递归删除非空目录（不应执行到这里）: " + ok2);
    	} catch (org.apache.hadoop.fs.PathIsNotEmptyDirectoryException e) {
        	System.out.println("非递归删除非空目录 d3 失败（符合预期）: Directory is not empty");
    	}

    	// (3) 递归删除：成功
    	boolean ok3 = fs.delete(d3, true);
    	System.out.println("递归删除非空目录 " + d3 + " : " + ok3);
	}
    /* ============ 8. 追加内容到开头或结尾 ============ */
    static void task8_append(String remote, String addFile) throws Exception {
        System.out.println("\n===== 8. 追加内容（开头或结尾）=====");
        Path path = new Path(remote);

        // (1) 追加到末尾
        FSDataOutputStream out = fs.append(path);
        BufferedReader br = new BufferedReader(new FileReader(addFile));
        String line;
        while ((line = br.readLine()) != null) {
            out.write((line + "\n").getBytes());
        }
        br.close();
        out.close();
        System.out.println("已追加到末尾：");
        printFile(path);

        // (2) 追加到开头
        FSDataInputStream in = fs.open(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();

        br = new BufferedReader(new FileReader(addFile));
        StringBuilder prefix = new StringBuilder();
        while ((line = br.readLine()) != null) {
            prefix.append(line).append("\n");
        }
        br.close();

        FSDataOutputStream out2 = fs.create(path, true);
        out2.write((prefix.toString() + sb.toString()).getBytes());
        out2.close();
        System.out.println("已追加到开头：");
        printFile(path);
    }

    static void printFile(Path p) throws Exception {
        FSDataInputStream in = fs.open(p);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println("    " + line);
        }
        br.close();
    }

    /* ============ 9. 删除文件 ============ */
    static void task9_deleteFile(String remote) throws Exception {
        System.out.println("\n===== 9. 删除文件 =====");
        Path path = new Path(remote);
        boolean ok = fs.delete(path, false);
        System.out.println("删除 " + remote + " : " + ok);
        System.out.println("文件是否还存在: " + fs.exists(path));
    }

    /* ============ 10. 移动文件 ============ */
    static void task10_move(String src, String dst, String localTxt, String base) throws Exception {
        System.out.println("\n===== 10. 移动/重命名 =====");
        Path srcPath = new Path(src);
        fs.mkdirs(srcPath.getParent());
        fs.copyFromLocalFile(false, true, new Path(localTxt), srcPath);
        System.out.println("准备文件: " + src);

        Path dstPath = new Path(dst);
        boolean ok = fs.rename(srcPath, dstPath);
        System.out.println("移动 " + src + " -> " + dst + " : " + ok);

        System.out.println("源路径是否存在: " + fs.exists(srcPath));
        System.out.println("目标路径是否存在: " + fs.exists(dstPath));
        System.out.println("目标文件内容：");
        printFile(dstPath);
    }
}
