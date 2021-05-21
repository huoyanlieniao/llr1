import java.util.*;

/**
 * @Author: sun
 * @Date: 2021/5/20 19:44
 * @title: llr1
 * @Description:
 * @version: 1.0
 */
public class llr1 {

    //声明
    //单个符号first集
    public static HashMap<Character, HashSet<Character>> firstSet = new HashMap<>();
    //符号串first集
    public static HashMap<String, HashSet<Character>> firstSetX = new HashMap<>();

    //开始符
    public static char S = 'S';
    public static HashMap<Character, HashSet<Character>> followSet = new HashMap<>();

    //非终结符，大写字母
    public static HashSet<Character> VnSet = new HashSet<>();
    //终结符，小写字母
    public static HashSet<Character> VtSet = new HashSet<>();

    //非终结符-产生式集合
    public static HashMap<Character, ArrayList<String>> experssionSet = new HashMap<>();

    //输入规则
    public static String[] inputExperssion = { "S->I", "S->o", "I->i(E)SL", "L->eS", "L->~", "E->a", "E->b"};

    //分析表
    public static String[][] table;

    //分析栈
    public static Stack<Character> analyzeStatck = new Stack<>();

    //输入字符串
    public static String strInput = "i(a)i(b)oeo$";
    //行为
    public static String action = "";
    //步骤
    public static int index = 0;


    public static void main(String[] args){
        getEnd();
        Init();
        createTable();
        output();
        analyzeLL();
    }


    /**
     * 单个字符的first集合
     * @date 2021/5/20 20:30
     * @author sun
     * @param s
     * @return void
     */
    public static void getFirst(char s){
        //单个字符的first集合
        if(firstSet.containsKey(s)){
            //如果已经有了则直接返回不需要执行
            return;
        }

        //临时添加
        HashSet<Character> set = new HashSet<>();

        if(VtSet.contains(s)){
            //如果是终结符也就是小写字母，其first级就是自身，可以直接添加
            //S->xA
            set.add(s);
            //将终结符添加到first集合
            firstSet.put(s,set);
            return;
        }

        //s是非终结符，也就是大写字母，则需要处理产生式里的每一个
        if(experssionSet.get(s)!=null){
            for(String c:experssionSet.get(s)){
                //S->~
                if(isEmpty(s)){
                    //如果是空，则将空的添加进去
                    set.add('~');

                }else{
                    //非终结符且不为空，则开始扫描判断添加，如果不含空就不再处理，为空则处理下一个
                    //S->ABC
                    for(char d:c.toCharArray()){
                        if(!firstSet.containsKey(d)){
                            //不在单个符号集中
                            //递归调用，判断单个字符
                            getFirst(d);
                        }

                        //找到本字符的单个符号集
                        HashSet<Character> curFirst = firstSet.get(d);
                        //添加
                        set.addAll(curFirst);

                        if(!curFirst.contains('~')){
                            //A!->~
                            //单个符号集不好含空就可以结束了
                            //包含空则需要判断下一个
                            //这里可以直接跳出
                            break;
                        }

                    }

                }

            }
        }

        //这里将三种情况都进判断，添加到
        firstSet.put(s, set);


    }

    /**
     * 字符串的first集
     * @date 2021/5/20 20:37
     * @author sun
     * @param s
     * @return void
     */
    public static void getFirst(String s) {
        //字符串first集中包含则不需要继续
        if (firstSetX.containsKey(s)){
            return;
        }

        HashSet<Character> set = new HashSet<>();

        // 从左往右扫描该式
        int i = 0;
        while (i < s.length()) {
            //逐个字符判断
            char cur = s.charAt(i);
            if (!firstSet.containsKey(cur)){
                //单个字符不包括则调用
                getFirst(cur);
            }

            //将单个字符的fitst集合取出来
            HashSet<Character> rightSet = firstSet.get(cur);

            // 将其非空 first集加入左部
            set.addAll(rightSet);
            // 若包含空串 处理下一个符号
            if (rightSet.contains('~')){
                i++;
            }else{
                break;
            }

            // 若到了尾部 即所有符号的first集都包含空串 把空串加入fisrt集
            if (i == s.length()) {
                set.add('~');
            }
        }
        firstSetX.put(s, set);
    }

    /**
     * 获取follow集
     * @date 2021/5/20 21:19
     * @author sun
     * @param c
     * @return void
     */
    public static void getFollow(char c) {
        //获取本字符的产生式
        ArrayList<String> list = experssionSet.get(c);
        //如果follow集包含则取其集
        HashSet<Character> leftFollowSet = followSet.containsKey(c) ? followSet.get(c) : new HashSet<>();

        //如果是开始符 添加 $
        if (c==S){
            leftFollowSet.add('$');
        }

        //查找输入的所有产生式，添加c的后跟终结符
        for (char ch:VnSet){
            for (String s:experssionSet.get(ch)){
                //本字符的产生式
                for (int i = 0;i<s.length();i++){
                    if(c == s.charAt(i) && i + 1 < s.length() && VtSet.contains(s.charAt(i + 1))){
                        //本字符和产生式相等，并且长度不超过，并且包含下一个字符
                        //就是找字符的下一位
                        leftFollowSet.add(s.charAt(i + 1));
                    }
                }
            }
        }
        followSet.put(c, leftFollowSet);

        //A->aB,A->aBc,first(c)中包含空，将follow(A)添加进去
        for (String s : list) {
            int i = s.length() - 1;
            while (i >= 0) {
                char cur = s.charAt(i);
                //只处理终结符
                if (VnSet.contains(cur)) {
                    // 都按 A->αBβ  形式处理
                    //1.若β不存在   followA 加入 followB
                    //2.若β存在，把β的非空first集  加入followB
                    //3.若β存在  且first(β)包含空串  followA 加入 followB
                    String right = s.substring(i + 1);
                    HashSet<Character> rightFirstSet;

                    if(!followSet.containsKey(cur)){
                        //follow集中不包含则递归调用
                        getFollow(cur);
                    }

                    HashSet<Character> curFollowSet = followSet.get(cur);
                    //先找出first(β),将非空的加入followB
                    if (0 == right.length()) {
                        //不存在，将followA加入followB
                        curFollowSet.addAll(leftFollowSet);

                    } else {
                        if (1 == right.length()) {
                            //存在，获取fitst集合
                            if(!firstSet.containsKey(right.charAt(0))){
                                //furst集中不包含本字符，则调用添加
                                getFirst(right.charAt(0));
                            }
                            //获取fitst集合
                            rightFirstSet = firstSet.get(right.charAt(0));

                        } else {
                            //长度不为1
                            if(!firstSetX.containsKey(right)){
                                getFirst(right);
                            }
                            rightFirstSet = firstSetX.get(right);
                        }

                        for(char var : rightFirstSet){
                            //将非空添加进去
                            if (!isEmpty(var)){
                                curFollowSet.add(var);
                            }
                        }

                        // 若first(β)包含空串,将followA加入followB
                        if(rightFirstSet.contains('~')){
                            curFollowSet.addAll(leftFollowSet);
                        }
                    }
                    followSet.put(cur, curFollowSet);
                }
                i--;
            }
        }
    }


    /**
     * 获取生成式和和非终结符的first及follow集
     * @date 2021/5/20 21:34
     * @author sun
     * @param
     * @return void
     */
    public static void Init() {
        //获取生成式
        for (String e:inputExperssion){
            //遍历输入规则
            //进行切割以->为分隔符
            String[] str = e.split("->");

            //获取非终结符
            char c = str[0].charAt(0);
            //如果产生式中包含则获取对应的产生式，如果没有则新建
            ArrayList<String> list = experssionSet.containsKey(c)? experssionSet.get(c):new ArrayList();
            list.add(str[1]);
            experssionSet.put(c,list);

        }

        //构造非终结符的first集
        for (char c:VnSet){
            getFirst(c);
        }
        //构造开始符的follow集
        getFollow(S);
        //构造非终结符的follow集
        for(char c:VnSet){
            getFollow(c);
        }


    }

    /**
     * 先求非终结符再求终结符，将相应的添加进去
     * 对终结符集合和非终结符集合初始化
     * @date 2021/5/20 21:45
     * @author sun
     * @param
     * @return void
     */
    public static void getEnd(){

        for(String e:inputExperssion){
            VnSet.add(e.split("->")[0].charAt(0));
        }
        for(String e:inputExperssion){
            for (char c:e.split("->")[1].toCharArray()){
                if (!VnSet.contains(c)){
                    VtSet.add(c);
                }
            }
        }

    }

    /**
     * 创建分析表
     * @date 2021/5/20 22:34
     * @author sun
     * @param
     * @return void
     */
    public static void createTable(){
        Object[] VtArray=VtSet.toArray();
        Object[] VnArray=VnSet.toArray();

        //预测表分析表初始化
        table = new String[VnArray.length+1][VtArray.length+1];
        table[0][0]="Vn/Vt";

        //初始化行与列
        for(int i=0;i<VtArray.length;i++){
            //终结符为空则为$，不为空则添加
            table[0][i+1]=(VtArray[i].toString().charAt(0)=='~')? "$":VtArray[i].toString();
        }
        for(int i=0;i<VnArray.length;i++){
            table[i+1][0]=VnArray[i]+"";
        }

        //全部设为error
        for(int i=0;i<VnArray.length;i++){
            for(int j=0;j<VtArray.length;j++){
                table[i+1][j+1]="error";
            }
        }

        //插入生成式
        //遍历非终结符
        for(char A:VnSet){
            //遍历产生式，获得本身的规则
            for(String s: experssionSet.get(A)){
                if(!firstSetX.containsKey(s)){
                    //规则没有first集合
                    //初始化
                    getFirst(s);
                }
                //获取first集
                HashSet<Character> set = firstSetX.get(s);

                for(char a:set){
                  insert(A,a,s);
                }

                if(set.contains('~')){
                    //包含空串则找follow集
                    HashSet<Character> setFollow = followSet.get(A);
                    if(setFollow.contains('$')){
                       insert(A,'$',s);
                    }
                    for(char b:setFollow){
                        insert(A,b,s);
                    }
                }

            }
        }


    }

    /**
     * 插入对应规则
     * @date 2021/5/20 22:13
     * @author sun
     * @param X
     * @param a1
     * @param s
     * @return void
     */
    public static void insert(char X, char a1, String s) {
        if(isEmpty(a1)){
            //如果为空
            a1='$';
        }
        //遍历非终结
        for(int i=0;i<VnSet.size();i++){
            if(table[i][0].charAt(0)==X){
                //如果行的相等
                for(int j=0;j<VtSet.size();j++){
                    //看列
                    if(table[0][j].charAt(0)==a1){
                        table[i][j]=s;
                        return;
                    }
                }

            }
        }
    }

    /**
     * 分析语法
     * @date 2021/5/20 23:04
     * @author sun
     * @param
     * @return void
     */
    public static void analyzeLL() {
        System.out.println("****************LL分析过程**********");
//        System.out.println("               Stack           Input     Action");

        //初始化栈
        analyzeStatck.push('$');
        analyzeStatck.push(S);

        //displayLL();

        //获取栈顶
        char X = analyzeStatck.peek();
        while (X != '$') {
            //栈顶不为$
            //获取字符
            char a = strInput.charAt(index);
            if (X == a) {
                //相等
                action = "匹配" + analyzeStatck.peek();
                //出栈
                analyzeStatck.pop();
                //获取下一个字符
                index++;
            } else if (VtSet.contains(X)){
                //如果终结符，结束
                action="error";
                displayLL();
                System.out.println("error at '" + strInput.charAt(index) + " in " + index);
                return;
            } else if (find(X, a).equals("error")){
                //如果是空则报错
                action="error";
                displayLL();
                System.out.println("error at '" + strInput.charAt(index) + " in " + index);
                return;
            } else if (find(X, a).equals("~")) {
                 //如果是~，则弹出栈
                analyzeStatck.pop();
                action = X + "->~";

            } else {
                String str = find(X, a);
                if (str != "") {
                    action = X + "->" + str;
                    analyzeStatck.pop();
                    int len = str.length();
                    for (int i = len - 1; i >= 0; i--){
                        //入栈
                        analyzeStatck.push(str.charAt(i));
                    }

                } else {
                    System.out.println("error at '" + strInput.charAt(index) + " in " + index);
                    return;
                }
            }
            //获取栈顶
            X = analyzeStatck.peek();
            displayLL();
        }
        System.out.println("analyze LL1 successfully");
        System.out.println("****************LL分析过程**********");
    }


    /**
     * 输出输入字符串集相关行为
     * @date 2021/5/20 22:40
     * @author sun
     * @param
     * @return void
     */
    public static void displayLL() {
        // 输出 LL1
        Stack<Character> s = analyzeStatck;
        //输出栈
        System.out.printf("%23s", s);
        //输出字符串截取
        System.out.printf("%13s", strInput.substring(index));
        //输出行为
        System.out.printf("%10s", action);

        System.out.println();
    }


    /**
     * 查找产生式
     * @date 2021/5/20 22:56
     * @author sun
     * @param X
     * @param a
     * @return java.lang.String
     */
    public static String find(char X,char a){
        for (int i=0;i<VnSet.size()+1;i++){
            if(table[i][0].charAt(0)==X){
                for(int j=0;j<VtSet.size()+1;j++){
                    if(table[0][j].charAt(0)==a){
                        return table[i][j];
                    }
                }
            }
        }
        return "";
    }


    /**
     * 判断是否是空
     * @date 2021/5/20 20:13
     * @author sun
     * @param s
     * @return boolean
     */
    public boolean isEmpty(String s){
        if(s.equals("~")){
            return true;
        }
        return false;
    }
    public static boolean isEmpty(char s){
        if("~".equals(s)){
            return true;
        }
        return false;
    }


    public static void output() {
        System.out.println("*********规则********");
        for(String s:inputExperssion){
            System.out.println(s);
        }
        System.out.println("*********规则********");


        System.out.println("*********输入字符串********");
        System.out.println(strInput);
        System.out.println("*********输入字符串********");



        System.out.println("*********first集********");
        for (Character c : VnSet) {
            HashSet<Character> set = firstSet.get(c);
            System.out.printf("%10s", c + "  ->   ");
            for (Character var : set){
                System.out.print(var);
            }
            System.out.println();
        }
        System.out.println("**********first集**********");

        System.out.println("*********firstX集********");
        Set<String> setStr = firstSetX.keySet();
        for (String s : setStr) {
            HashSet<Character> set = firstSetX.get(s);
            System.out.printf("%10s", s + "  ->   ");
            for (Character var : set){
                System.out.print(var);
            }
            System.out.println();
        }
        System.out.println("**********firstX集**********");

        System.out.println("**********follow集*********");

        for (Character c : VnSet) {
            HashSet<Character> set = followSet.get(c);
            System.out.print("Follow " + c + ":");
            for (Character var : set){
                System.out.print(var);
            }
            System.out.println();
        }
        System.out.println("**********follow集**********");

        System.out.println("**********LL1预测分析表********");

        for (int i = 0; i < VnSet.size() + 1; i++) {
            for (int j = 0; j < VtSet.size() + 1; j++) {
                System.out.printf("%6s", table[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("**********LL1预测分析表********");


    }

}





