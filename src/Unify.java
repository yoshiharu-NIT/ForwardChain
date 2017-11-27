/***
 Unify Program written



 変数:前に？をつける．  

 Examle:
 % Unify "Takayuki" "Takayuki"
 true

 % Unify "Takayuki" "Takoyuki"
 false

 % Unify "?x am Takayuki" "I am Takayuki"
 ?x = I .

 % Unify "?x is ?x" "a is b"
 false

 % Unify "?x is ?x" "a is a"
 ?x = a .

 % Unify "?x is a" "b is ?y"
 ?x = b.
 ?y = a.

 % Unify "?x is a" "?y is ?x"
 ?x = a.
 ?y = a.

 Unify は，ユニフィケーション照合アルゴリズムを実現し，
 パターン表現を比較して矛盾のない代入によって同一と判断
 できるかどうかを調べる．

 ポイント！
 ここでは，ストリング同士の単一化であるから，出現検査を行う必要はない．
 しかし，"?x is a"という表記を"is(?x,a)"とするなど，構造を使うならば，
 単一化において出現検査を行う必要がある．
 例えば，"a(?x)"と"?x"を単一化すると ?x = a(a(a(...))) となり，
 無限ループに陥ってしまう．

 ***/

import java.util.*;
import java.io.*;

class Unify {

    static ArrayList<String> ansList = new ArrayList<>();
    static String keyset;

    public static void main(String arg[]) {
        if (arg.length == 1) { // ファイル名のみの指定のとき
            System.out.println("Usage : % [filename] [string]");
        } else {
            String fileName = arg[0]; // 実行時の第1引数でファイル名指定
            String pattern[] = new String[arg.length];
            for (int i = 1; i < arg.length; i++) { // 第2引数以降をパターンとする
                pattern[i] = arg[i];
            }
            match(fileName, pattern, 1, new HashMap<String, String>(), arg.length);
            System.out.println("("+ keyset.substring(1, keyset.length() - 1) + ") = {"+ ansList.toString().substring(1, ansList.toString().length() - 1) + "}");
        }
    }

    /***
     * patternとデータベースの全データをマッチング
     * @param fileName ファイル名
     * @param pattern マッチングさせるパターンの配列
     * @param i 配列のインデックス
     * @param vars 変数束縛を保管するハッシュマップ
     * @param fin 最後の引数のインデックス
     */
    public static void match(String fileName, String pattern[], int i, HashMap<String, String> vars, int fin) {
        try { // ファイル読み込みに失敗したときの例外処理のためのtry-catch構文

            if (i == fin) { // 再帰の終了条件
                keyset = vars.keySet().toString(); // キーの順番を保持
                // 変数束縛の集合の要素として追加
                ansList.add("("+ vars.values().toString().substring(1, vars.values().toString().length() - 1) + ")");
            } else {
                // 文字コードを指定してBufferedReaderオブジェクトを作る
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

                // ファイルから変数lineに1行ずつ読み込むfor文
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    Unifier unifier = new Unifier();
                    if (i == 1) {
                        if (unifier.unify(pattern[i], line, unifier.vars)) { // パターンとlineのマッチング成功
                            match(fileName, pattern, i + 1, unifier.vars, fin); // 次のパターンについて再帰的に実行
                        }
                    } else {
                        // マッチングする前の変数束縛をコピー
                        HashMap<String, String> mem = new HashMap<String, String>(vars);
                        if (unifier.unify(pattern[i], line, mem)) { // パターンとlineのマッチング成功
                            match(fileName, pattern, i + 1, unifier.vars, fin); // 次のパターンについて再帰的に実行
                        }
                        unifier.vars = mem; // 新しい変数束縛をする前のハッシュマップに書き換え
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace(); // 例外が発生した所までのスタックトレースを表示
        }
    }
}

class Unifier {
    StringTokenizer st1;
    String buffer1[];
    StringTokenizer st2;
    String buffer2[];
    HashMap<String, String> vars;

    Unifier() {
        vars = new HashMap<String, String>();
    }

    public boolean unify(String string1, String string2, HashMap<String, String> bindings) {
        this.vars = bindings;
        return unify(string1, string2);
    }

    public boolean unify(String string1, String string2) {
//		System.out.println(string1);
//		System.out.println(string2);

        // 同じなら成功
        if (string1.equals(string2))
            return true;

        // 各々トークンに分ける
        st1 = new StringTokenizer(string1);
        st2 = new StringTokenizer(string2);

        // 数が異なったら失敗
        if (st1.countTokens() != st2.countTokens())
            return false;

        // 定数同士
        int length = st1.countTokens();
        buffer1 = new String[length];
        buffer2 = new String[length];
        for (int i = 0; i < length; i++) {
            buffer1[i] = st1.nextToken();
            buffer2[i] = st2.nextToken();
        }
        for (int i = 0; i < length; i++) {
            if (!tokenMatching(buffer1[i], buffer2[i])) { // トークン毎にマッチングを行う
                return false;
            }
        }

        // 最後まで O.K. なら成功
        return true;
    }

    boolean tokenMatching(String token1, String token2) {
        if (token1.equals(token2))
            return true;
        if (var(token1) && !var(token2))
            return varMatching(token1, token2);
        if (!var(token1) && var(token2))
            return varMatching(token2, token1);
        if (var(token1) && var(token2))
            return varMatching(token1, token2);
        return false;
    }

    boolean varMatching(String vartoken, String token) {
        if (vars.containsKey(vartoken)) { // vartokenが既に変数束縛されている
            if (token.equals(vars.get(vartoken))) { // tokenと変数束縛の値が同じ
                return true;
            } else {
                return false;
            }
        } else {
            replaceBuffer(vartoken, token); // bufferに存在する同じ名前の変数を全て定数に置き換える
            if (vars.containsValue(vartoken)) { // vartokenがvalueとして格納されている
                replaceBindings(vartoken, token); // 変数情報の変更
            }
            vars.put(vartoken, token);
        }
        return true;
    }

    void replaceBuffer(String preString, String postString) {
        for (int i = 0; i < buffer1.length; i++) {
            if (preString.equals(buffer1[i])) {
                buffer1[i] = postString;
            }
            if (preString.equals(buffer2[i])) {
                buffer2[i] = postString;
            }
        }
    }

    void replaceBindings(String preString, String postString) {
        Iterator<String> keys;
        for (keys = vars.keySet().iterator(); keys.hasNext();) {
            String key = (String) keys.next();
            if (preString.equals(vars.get(key))) {
                vars.put(key, postString);
            }
        }
    }

    boolean var(String str1) {
        // 先頭が ? なら変数
        return str1.startsWith("?");
    }

}
