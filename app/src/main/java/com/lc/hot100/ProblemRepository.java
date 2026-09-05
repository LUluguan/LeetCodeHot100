package com.lc.hot100;

import android.content.Context;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 题库仓库：负责从 assets 加载 100 道题目的元信息、描述、答案代码与解析。
 * 数据文件：
 *   assets/index.json   —— 分类与题目元信息（题号、标题、难度）
 *   assets/desc.txt     —— 题目描述，以 "###题号" 分段
 *   assets/code1~5.txt  —— 答案代码，以 "###题号" 分段
 *   assets/explain1~2.txt —— 思路解析，以 "###题号" 分段
 */
public class ProblemRepository {

    /** 单道题目 */
    public static class Problem {
        public int num;
        public String title;
        public String difficulty;
        public String category;
        public String desc = "";
        public String codeJava = "";
        public String codeC = "";
        public String codePy = "";
        public String explain = "";
    }

    /** 一个分类（如"哈希"）及其下的题目列表 */
    public static class Category {
        public String name;
        public final List<Problem> problems = new ArrayList<>();
    }

    private static final Pattern MARKER = Pattern.compile("^###(\\d+)\\s*$");

    private static ProblemRepository sInstance;

    private final List<Category> mCategories = new ArrayList<>();
    private final Map<Integer, Problem> mProblemMap = new HashMap<>();

    private ProblemRepository(Context context) throws Exception {
        String index = readAsset(context, "index.json");
        JSONObject root = new JSONObject(index);
        JSONArray cats = root.getJSONArray("categories");

        Map<Integer, String> descs = new HashMap<>();
        Map<Integer, String> codeJava = new HashMap<>();
        Map<Integer, String> codeC = new HashMap<>();
        Map<Integer, String> codePy = new HashMap<>();
        Map<Integer, String> explains = new HashMap<>();
        for (String f : new String[]{"desc1.txt", "desc2.txt",
                "code1.txt", "code2.txt", "code3.txt", "code4.txt", "code5.txt",
                "code_c1.txt", "code_c2.txt", "code_c3.txt", "code_c4.txt", "code_c5.txt",
                "code_py1.txt", "code_py2.txt", "code_py3.txt", "code_py4.txt", "code_py5.txt",
                "explain1.txt", "explain2.txt"}) {
            String type;
            if (f.startsWith("desc")) type = "desc";
            else if (f.startsWith("code_c")) type = "c";
            else if (f.startsWith("code_py")) type = "py";
            else if (f.startsWith("code")) type = "java";
            else type = "explain";
            Map<Integer, String> target;
            switch (type) {
                case "desc": target = descs; break;
                case "c": target = codeC; break;
                case "py": target = codePy; break;
                case "java": target = codeJava; break;
                default: target = explains; break;
            }
            target.putAll(parseMarkedFile(readAsset(context, f)));
        }

        for (int i = 0; i < cats.length(); i++) {
            JSONObject cat = cats.getJSONObject(i);
            Category c = new Category();
            c.name = cat.getString("name");
            JSONArray ps = cat.getJSONArray("problems");
            for (int j = 0; j < ps.length(); j++) {
                JSONObject p = ps.getJSONObject(j);
                Problem problem = new Problem();
                problem.num = p.getInt("num");
                problem.title = p.getString("title");
                problem.difficulty = p.getString("difficulty");
                problem.category = c.name;
                problem.desc = getOrMissing(descs, problem.num);
                problem.codeJava = getOrMissing(codeJava, problem.num);
                problem.codeC = getOrMissing(codeC, problem.num);
                problem.codePy = getOrMissing(codePy, problem.num);
                problem.explain = getOrMissing(explains, problem.num);
                c.problems.add(problem);
                mProblemMap.put(problem.num, problem);
            }
            mCategories.add(c);
        }
    }

    private static String getOrMissing(Map<Integer, String> map, int num) {
        String v = map.get(num);
        return v == null ? "（内容缺失，请检查 assets 数据文件）" : v;
    }

    /** 后台线程加载题库，完成后在调用线程回调 */
    public static void load(Context context, final Runnable onDone) {
        final Context app = context.getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sInstance = new ProblemRepository(app);
                } catch (Exception e) {
                    sInstance = new ProblemRepository(); // 空题库兜底，避免崩溃
                }
                if (onDone != null) {
                    onDone.run();
                }
            }
        }).start();
    }

    /** 空构造：加载失败时兜底 */
    private ProblemRepository() {
    }

    public static synchronized ProblemRepository get() {
        return sInstance;
    }

    public List<Category> getCategories() {
        return mCategories;
    }

    public Problem findByNum(int num) {
        return mProblemMap.get(num);
    }

    public int totalProblems() {
        return mProblemMap.size();
    }

    /** 解析 "###题号" 分段的文本文件 */
    private static Map<Integer, String> parseMarkedFile(String content) {
        Map<Integer, String> result = new HashMap<>();
        if (content == null || content.isEmpty()) {
            return result;
        }
        int currentNum = -1;
        StringBuilder sb = new StringBuilder();
        String[] lines = content.split("\n", -1);
        for (String line : lines) {
            Matcher m = MARKER.matcher(line.trim());
            if (m.matches()) {
                if (currentNum > 0) {
                    result.put(currentNum, sb.toString().trim());
                }
                currentNum = Integer.parseInt(m.group(1));
                sb.setLength(0);
            } else if (currentNum > 0) {
                sb.append(line).append('\n');
            }
        }
        if (currentNum > 0) {
            result.put(currentNum, sb.toString().trim());
        }
        return result;
    }

    private static String readAsset(Context context, String name) throws Exception {
        InputStream is = context.getAssets().open(name);
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8));
            char[] buf = new char[8192];
            int n;
            while ((n = reader.read(buf)) > 0) {
                sb.append(buf, 0, n);
            }
            return sb.toString();
        } finally {
            is.close();
        }
    }

    /** 按难度返回强调色（LeetCode 配色） */
    public static int difficultyColor(String difficulty) {
        if (difficulty == null) {
            return Color.GRAY;
        }
        switch (difficulty) {
            case "简单":
                return Color.parseColor("#2DB55D");
            case "中等":
                return Color.parseColor("#FFA116");
            case "困难":
                return Color.parseColor("#EF4743");
            default:
                return Color.GRAY;
        }
    }

    /** 按难度返回浅色背景 */
    public static int difficultyBg(String difficulty) {
        if (difficulty == null) {
            return Color.LTGRAY;
        }
        switch (difficulty) {
            case "简单":
                return Color.parseColor("#E6F6EC");
            case "中等":
                return Color.parseColor("#FFF3E5");
            case "困难":
                return Color.parseColor("#FDEBEA");
            default:
                return Color.LTGRAY;
        }
    }
}
