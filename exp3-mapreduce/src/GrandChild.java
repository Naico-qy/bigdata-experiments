import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class GrandChild {

    public static class GrandChildMapper extends Mapper<Object, Text, Text, Text> {
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString();
            if (line.startsWith("child")) return; // 跳过表头
            
            String[] parts = line.split("\\s+");
            if (parts.length >= 2) {
                String child = parts[0];
                String parent = parts[1];
                // 输出: 孩子->父母 (用于找祖父母)
                context.write(new Text(child), new Text("p:" + parent));
                // 输出: 父母->孩子 (用于找孙子)
                context.write(new Text(parent), new Text("c:" + child));
            }
        }
    }

    public static class GrandChildReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            List<String> children = new ArrayList<>();
            List<String> parents = new ArrayList<>();
            
            for (Text val : values) {
                String v = val.toString();
                if (v.startsWith("c:")) {
                    children.add(v.substring(2));
                } else if (v.startsWith("p:")) {
                    parents.add(v.substring(2));
                }
            }
            
            // 如果这个人是父母，那么他的孩子就是他的孙子/孙女的父母
            // 我们需要找到: 孙子 -> 祖父母
            // 当前key是父母，children是他的孩子，parents是他的父母
            // 所以对于每个child，每个parent都是他的祖父母
            for (String child : children) {
                for (String parent : parents) {
                    context.write(new Text(child), new Text(parent));
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "grandchild");
        job.setJarByClass(GrandChild.class);
        job.setMapperClass(GrandChildMapper.class);
        job.setReducerClass(GrandChildReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
