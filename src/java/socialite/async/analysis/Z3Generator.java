package socialite.async.analysis;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import socialite.async.codegen.AsyncCodeGen;
import socialite.util.MySTGroupFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Z3Generator {
    String gFuncExpr;
    String fFuncExpr;

    public Z3Generator(String gFuncExpr, String fFuncExpr) {
        this.gFuncExpr = gFuncExpr;
        this.fFuncExpr = fFuncExpr;
    }

    String[] findVarInExpr(String expr) {
        Pattern pattern = Pattern.compile("[a-zA-Z]\\w*");
        Matcher matcher = pattern.matcher(expr);
        List<String> varList = new ArrayList<>();
        while (matcher.find()) {
            String varName = matcher.group(0);
            varList.add(varName);
        }
        if (varList.size() > 0)
            return varList.toArray(new String[varList.size()]);
        return null;
    }

    String generate() {
        String[] gVarList = findVarInExpr(gFuncExpr);
        String[] fVarList = findVarInExpr(fFuncExpr);
        STGroup stg = new MySTGroupFile(AsyncCodeGen.class.getResource("Z3.stg"),
                "UTF-8", '<', '>');
        stg.load();
        ST st = stg.getInstanceOf("Convert");
        st.add("gVarList", gVarList);
        st.add("gFuncExpr", gFuncExpr);
        st.add("fVarList", fVarList);
        st.add("fFuncExpr", fFuncExpr);
        return st.render();
    }

    public static void main(String[] args) {
        Z3Generator z3Generator = new Z3Generator("(+ (+ a b) 0.15)", "(/ (* 0.85 r1) d)");
        System.out.println(z3Generator.generate());
//        (/ (* 0.85 r1) d)
//        (+ (+ a b) 0.15)
    }
}
