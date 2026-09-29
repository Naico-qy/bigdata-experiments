# 大数据系统基本实验

- **学号**: 202400390011
- **姓名**: 武庆阳
- **课程**: 大数据系统基本实验
- **环境**: Hadoop 3.3.5 + Ubuntu 22.04 (WSL2)

## 实验内容

| 实验 | 内容 | 路径 |
|------|------|------|
| 实验零 | Hadoop伪分布式安装与配置 |  |
| 实验一 | HDFS Shell命令操作 | `exp1-hdfs-shell/` |
| 实验二 | Java HDFS API编程 | `exp2-hdfs-api/` |
| 实验三 | MapReduce编程（去重/排序/祖孙关系） | `exp3-mapreduce/` |

## MapReduce实验详解

### 任务1: 文件合并与去重 (Dedup.java)
- 输入: fileA.txt, fileB.txt（日期-用户记录）
- 输出: 9条去重后的唯一记录
- 命令: `hadoop jar dedup.jar Dedup /user/hadoop/exp3/input /user/hadoop/exp3/dedup_output`

### 任务2: 数据排序 (Sort.java)
- 输入: sort1.txt, sort2.txt, sort3.txt（数字列表）
- 输出: 11条升序排名记录
- 命令: `hadoop jar sort.jar Sort /user/hadoop/exp3/sort_input /user/hadoop/exp3/sort_output`

### 任务3: 祖孙关系挖掘 (GrandChild.java)
- 输入: family.txt（child-parent关系表）
- 输出: 12条祖孙关系记录
- 命令: `hadoop jar grandchild.jar GrandChild /user/hadoop/exp3/family_input /user/hadoop/exp3/grandchild_output`

## 技术栈

- Hadoop 3.3.5
- Java 11
- Git
- WSL2 Ubuntu 22.04
