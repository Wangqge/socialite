package socialite.async.analysis;


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.HashMap;
import com.microsoft.z3.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.org.apache.xpath.internal.operations.Variable;
import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.apache.hadoop.net.ConnectTimeoutException;
import org.python.antlr.ast.Return;
import socialite.functions.Str;
//import sun.plugin2.message.Conversation;

import javax.naming.CompositeName;
import javax.swing.text.SimpleAttributeSet;

public class Z3Analysis {

    String nonaggrFunc=null;
    String aggrFunc=null;
    String []variables=null;
    double []consts =null;
    ArrayList<String> opt=null;
    Stack stack=null;
    int position;
    Context context;
    boolean orderIndenpent;
    RealExpr tmporder;
    public Z3Analysis(String [] _variables, double[] _consts,String _nonaggr,String _aggr){
        nonaggrFunc=_nonaggr;
        aggrFunc=_aggr;
        consts=_consts;
        variables=_variables;
        opt=new ArrayList<String>();
        opt.add("+");opt.add("-");opt.add("*");opt.add("/");
       // opt =new String[]{"(",")","+","-","*","/"};//const  TOLIST                       SET BY WANGQG
        stack =new Stack();
        position=0;
        orderIndenpent=false;
        HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("model", "true");
        cfg.put("proof","true");
        context = new Context(cfg);
    }


//    private void initnonaggr(){
//        int positions=0;
//        while (positions!=-1)
//            position=nonaggrFunc.indexOf("*");
//        }
//    }

    private RealExpr NonAggrfun(RealExpr input){
            RealExpr result=null;
            result=genExpr(input);
            position=0;
//System.out.println(result);
            return result;

    }
    private RealExpr Aggrfun(RealExpr x,RealExpr y){
        RealExpr result=null;
        if( aggrFunc.indexOf("sum")!=-1){
            result=sum(x,y);

        }
        else if (aggrFunc.indexOf("max")!=-1){
            result=max(x,y);
        }
        else if(aggrFunc.indexOf("min")!=-1){
            result=min(x,y);
        }else{
            System.out.println("error");
        }
        return result;
    }
    private RealExpr genaggrExpr(RealExpr input){
        int step=0;
        String tmpopt=null;
        RealExpr result=null,tp=null;
        step=aggrFunc.substring(aggrFunc.indexOf("$dsum")).indexOf(")");
        if(step==aggrFunc.length()-1){
            result=input;
            return result;
        }
        step++;
        if (opt.contains(aggrFunc.substring(step,step+1))){
                tmpopt=aggrFunc.substring(step,step+1);
            step++;
        }
        for(int i=0;i<consts.length;i++){
            if (aggrFunc.substring(step).startsWith(Double.toString(consts[i]))){
                tp=context.mkReal(Double.toString(consts[i]));
            }
        }
        if (tmpopt!=null)
        switch (tmpopt){
            case "+":
                result=(RealExpr) context.mkAdd(input,tp);
                break;
            case "-":
                result=(RealExpr) context.mkSub(input,tp);
                break;
            case "*":
                result=(RealExpr) context.mkMul(input,tp);
                break;
            case "/":
                result=(RealExpr) context.mkDiv(input,tp);
                break;
        }
        System.out.println(result);
        return result;
    }

    private RealExpr genExpr(RealExpr input){
        RealExpr result=null;
        RealExpr tmp1=null;
        RealExpr tmp2=null;
        boolean consttag=false;
        String  tmpopt=new String(")");

        if(nonaggrFunc.substring(position,position+1).equals("(")){//when input is {"(",")"} think as a expr;

            stack.push("(");
            position++;
            tmp1=genExpr(input);
           // System.out.println(position);
            for(int i=0;i<4;i++){
                if (opt.get(i).equals(nonaggrFunc.substring(position,position+1))){
                    tmpopt=opt.get(i);
                }
            }
            position++;
            if(!tmpopt.equals(")")){
               // System.out.println("usage:"+tmpopt);
            tmp2=genExpr(input);
            }
                if (tmpopt.equals(")")){
                    stack.pop();
                    result=tmp1;
                }
                else {
                    switch (tmpopt){
                        case "+":
                            result=add(tmp1,tmp2);
                            break;
                        case "-":
                            result=sub(tmp1,tmp2);
                            break;
                        case "*":
                           // System.out.println("*");
                            result=mul(tmp1,tmp2);
                            break;
                        case "/":
                            result=div(tmp1,tmp2);
                    }
                    if(nonaggrFunc.substring(position,position+1).equals(")")){
                        position++;
                        stack.pop();
                    }
                }
        }
        else{// consts of a variable
           // while(position<nonaggrFunc.length()){
                for (int i=0;i<consts.length;i++){//consts
                  if(nonaggrFunc.substring(position).indexOf(Double.toString(consts[i]))==0){
                      //  System.out.println("con");
                        result=context.mkReal(Double.toString(consts[i]));
                        position+=Double.toString(consts[i]).length();
                        consttag=true;
                 }
                }

                 if (consttag==false){//variable
                   //System.out.println("var");
                   for (int i=0;i<variables.length;i++){
                      if (nonaggrFunc.substring(position).indexOf(variables[i])==0){
                         if (nonaggrFunc.substring(position,position+2).equals("__")){
                                result = context.mkReal("1.0");
                             position+=variables[i].length();
                           }
//                           else if(nonaggrFunc.substring(position,position+1).equals("$")){
//                                if(nonaggrFunc.substring(position,position+5).equals("$dsum")){
//                                 //TODO
//                                 result=input;
//                                 position+=5;
//                             }
//
//                            }
                            else{
                                result=input;//context.mkRealConst(variables[i]);
                                position+=variables[i].length();
                            }
                        }
                    }
                }
//                if (opt.contains(nonaggrFunc.substring(position,position+1))){
//                    position++;
//                    tmpopt=nonaggrFunc.substring(position,position+1);
//                    genExpr()
//                }

            }
      //  }

        return result;

    }
    private String translate(String input ){
        StringBuilder tmp;
      //  System.out.println(input);
        Stack<String> varstk=new Stack<String>();
        Stack<String> opstk=new Stack<String>();
        Stack<String> containstk=new Stack<String>();
        ArrayList<String> vars=new ArrayList<>();
        for(int i=0;i<variables.length;i++){
            vars.add(variables[i]);
        }
        for (int i=0;i<consts.length;i++){
            vars.add(Double.toString(consts[i]));
        }
//        for(int i=0;i<vars.size();i++){
//            System.out.print(vars.get(i));
//        }

        for(int i=0;i<input.length();i++){
            if(opt.contains(input.substring(i,i+1))){
                if(input.substring(i,i+1).equals("+")||input.substring(i,i+1).equals("+")){
                    if (opstk.size()>1&&(opstk.peek().equals("+")||opstk.peek().equals("-"))){
                            String left=varstk.peek();varstk.pop();
                            String right=varstk.peek();varstk.pop();
                            varstk.push("("+left+opstk.peek()+right+")");
                            opstk.pop();
                    }
                    opstk.push(input.substring(i,i+1));
                }
                else{
                    opstk.push(input.substring(i,i+1));

                }
            }
            else if(input.equals("(")){
                containstk.push("(");

            }
            else if(input.equals(")")){
                String left=varstk.peek();varstk.pop();
                String right=varstk.peek();varstk.pop();
                containstk.push("("+left+opstk.pop()+right+")");
                containstk.pop();

            }
            else{
             //   System.out.print("in"+i);
                String crntvar=null;
                String left=null;
                for (int j=0;j<vars.size();j++){
                    if (input.substring(i).startsWith(vars.get(j))){
                        crntvar=vars.get(j);

                    }
                }
                if(opstk.empty()){//如果沒有操作符，是第一個元素
                        varstk.push(crntvar);
                }
                else if(opstk.peek().equals("*")||opstk.peek().equals("/")){//如果當前操作符號是懲處 直接做計算
                    if(!containstk.empty()){
                              varstk.push(crntvar);
                    }
                    else{//如果不是
                       // System.out.println("*use");
                        left=varstk.peek();
                        varstk.pop();
                        varstk.push("("+left+opstk.peek()+crntvar+")");
                     //   System.out.println("("+left+opstk.peek()+crntvar+")");
                        opstk.pop();
                    }
                }
                    i=i+crntvar.length()-1;
            }
        }
        String result=null;
        String s1=null;
        String s2=null;
        if(!opstk.empty()){
           // if(varstk.size()>=2){
            s1=varstk.peek();varstk.pop();
            s2=varstk.peek();varstk.pop();
//            }
//            else{
//                System.out.println("cant conver");
//                return input;
//            }
            result="("+s1+opstk.peek()+s2+")";
            opstk.pop();

        }
        else result=varstk.peek();
        position=0;
        System.out.println(result);
        return result;
    }
    public boolean chkcommunity(){
        RealExpr left=null;
        RealExpr right=null;
        BoolExpr result=null;
        BoolExpr test=null;
        RealExpr x[]=new RealExpr[2];
        x[0] = context.mkRealConst("x_1");
        x[1] = context.mkRealConst("x_2");
        left=Aggrfun(x[0],x[1]);
        right=Aggrfun(x[1],x[0]);

        test=context.mkEq(left,right);
        result = context.mkForall(x, test, 1, null, null, context.mkSymbol("q1"), context.mkSymbol("skid1"));
       // System.out.println(result.toString());
        Solver solver=context.mkSolver();
        solver.add(result);
        if(solver.check()==Status.SATISFIABLE){
            return true;
        }else{
            return false;
        }


    }
    public boolean chkassociativity(){
        RealExpr left=null;
        RealExpr right=null;
        BoolExpr result=null;
        BoolExpr test=null;
        RealExpr x[]=new RealExpr[3];
        x[0] = context.mkRealConst("x_1");
        x[1] = context.mkRealConst("x_2");
        x[2] =context.mkRealConst("x_3");
        left=Aggrfun(Aggrfun(x[0],x[1]),x[2]);
        right=Aggrfun(x[0],Aggrfun(x[1],x[2]));
        test=context.mkEq(left,right);
        result = context.mkForall(x, test, 1, null, null, context.mkSymbol("q1"), context.mkSymbol("skid1"));
      //  System.out.println(result.toString());
        Solver solver=context.mkSolver();
        solver.add(result);
        if(solver.check()==Status.SATISFIABLE){
            return true;
        }else{
            return false;
        }
    }
    public boolean chkorderInde(){
        RealExpr left=null;
        RealExpr right=null;
        BoolExpr result=null;
        BoolExpr test=null;
        RealExpr x[]=new RealExpr[2];
        x[0] = context.mkRealConst("x_1");
        x[1] = context.mkRealConst("x_2");
        //System.out.println(nonaggrFunc+NonAggrfun(x[0]));
        left=NonAggrfun(Aggrfun(x[0],x[1]));
        right=Aggrfun(NonAggrfun(x[0]),NonAggrfun(x[1]));

        //System.out.println(left.toString()+right.toString());
        test=context.mkEq(left,right);
        System.out.println(test);
        result = context.mkForall(x, test, 1, null, null, null,null);
        System.out.println(result);
        Solver solver=context.mkSolver();
        solver.add(result);
//        solver.add(context.mkGt(x[0],context.mkReal("0.0")));
//        solver.add(context.mkGt(x[1],context.mkReal("0.0")));
        if(solver.check()==Status.SATISFIABLE){
            return true;
        }else{
            return false;
        }

    }
    public boolean chkcontractability(){
        RealExpr left=null;
        RealExpr right=null;
        BoolExpr result=null;
        BoolExpr test1=null;
        RealExpr x[]=new RealExpr[1];
        x[0] = context.mkRealConst("x_1");
        //x[1] = context.mkRealConst("x_2");
        left=x[0];
        right=NonAggrfun(x[0]);

        test1=context.mkGe(left,right);
       // result = context.mkForall(x, test1, 1, null, null, context.mkSymbol("q1"), context.mkSymbol("skid1"));
        //System.out.println(test1.toString());
        Solver solver=context.mkSolver();
        solver.add(context.mkGt(x[0],context.mkReal("0.0")));
        solver.add(test1);
        if(solver.check()==Status.SATISFIABLE){
            return true;
        }else{
            return false;
        }

    }
    public RealExpr chkconvertable(){//TODO
        RealExpr left=null;
        RealExpr right=null;
        BoolExpr result=null,result1=null,result2=null;
        BoolExpr test1=null;
        BoolExpr test2=null;
        Sort[] types = new Sort[2];
        RealExpr x[]=new RealExpr[2];
        Symbol []syb=new Symbol[2];

        x[0] = context.mkRealConst("x_1");
        x[1] = context.mkRealConst("x_2");
        RealExpr c = context.mkRealConst("c");
        types[0]=context.getRealSort();
        types[1]=context.getRealSort();
        FuncDecl g1 = context.mkFuncDecl("g1", types, types[1]);
        Expr []  nonexpr=new Expr[2];
        nonexpr[0]=NonAggrfun(x[0]);
        nonexpr[1]=NonAggrfun(x[1]);
        right=(RealExpr) g1.apply(nonexpr);
        left=NonAggrfun((RealExpr)g1.apply(x));
        test1=context.mkEq(right,left);
        result1=context.mkForall(x,test1,1,null,null, context.mkSymbol("Q1"), context.mkSymbol("skid1"));
        right=(RealExpr) context.mkAdd((RealExpr)g1.apply(x),c);
        left=genaggrExpr(Aggrfun(x[0],x[1]));
        test2=context.mkEq(left,right);
        result2=context.mkForall(x,test2,1,null,null,null,null);
        Solver solver=context.mkSolver();
        solver.add(result1);
        solver.add(result2);
        solver.add(context.mkLt(context.mkReal("0.0"),c));
        solver.add(context.mkGt(context.mkReal("1.0"),c));
        System.out.println(solver.toString());
        System.out.println(solver.check());
        Model  s=null;
        RealExpr output=null;
        if(solver.check()==Status.SATISFIABLE){
            System.out.println(solver.getModel());
              s=solver.getModel();
        output=(RealExpr)s.evaluate(g1.apply(x),false);
        System.out.println(output.isAdd());
        return output;
        }
        else{
            return output;
        }
    }

    public boolean chkall() {

        RealExpr x[]=new RealExpr[3];
        x[0] = context.mkRealConst("x_1");
        x[1] = context.mkRealConst("x_2");
        x[2] = context.mkRealConst("x_3");
        System.out.println(mul(mul(x[0],x[1]),x[2]));

        boolean commutative=false;
        boolean monotonic=false;
        boolean order=false;
        boolean associativity=false;
        commutative=chkcommunity();System.out.println("finish1");
        associativity=chkassociativity();System.out.println("finish2");
        monotonic=chkcontractability();System.out.println("finish3");
        order=chkorderInde();System.out.println("finish4");

        System.out.println("commutative:"+commutative+"\nassociativity:"+associativity+"\norderIndenpend:"+order+"\nmonotonic"+monotonic);

        return(commutative&&monotonic&&order&&associativity);


    }
    public boolean Modelcheck(){
        RealExpr x[]=new RealExpr[3];
        x[0] = context.mkRealConst("x_1");
        x[1] = context.mkRealConst("x_2");
        x[2] = context.mkRealConst("x_3");
        System.out.println(mul(mul(x[0],x[1]),x[2]));

        boolean commutative=false;
        boolean monotonic=false;
        boolean order=false;
        boolean associativity=false;
        commutative=chkcommunity();System.out.println("finish1");
        associativity=chkassociativity();System.out.println("finish2");
        monotonic=chkcontractability();System.out.println("finish3");
        order=chkorderInde();System.out.println("finish4");

        System.out.println("commutative:"+commutative+"\nassociativity:"+associativity+"\norderIndenpend:"+order+"\nmonotonic:"+monotonic);

        return(commutative&&monotonic&&order&&associativity);

    }


    public String test1(String input) {
//System.out.println("heheheheheheh");
        String tmpnonaggr=nonaggrFunc;
        String tmpaggr=null;
        String result=null;
       nonaggrFunc=  translate(nonaggrFunc);
        RealExpr model=null;
        model=chkconvertable();
        if(model!=null){
            if (model.isAdd()){
                tmpaggr=aggrFunc.substring(aggrFunc.indexOf("$"),aggrFunc.substring(aggrFunc.indexOf("$")).indexOf(")")+1);

            result=input.replace(aggrFunc,tmpaggr);
            System.out.println(aggrFunc+tmpaggr+result);
            }
        }
        return result;
    }

    private RealExpr max(ArithExpr x, ArithExpr y) throws Z3Exception {
        return (RealExpr) context.mkITE(context.mkLe(x, y), y, x);
    }

    private RealExpr sum( ArithExpr x, ArithExpr y) throws Z3Exception {
        return (RealExpr) context.mkAdd(x,y);
    }

    private RealExpr min(ArithExpr x, ArithExpr y) throws Z3Exception {
        return (RealExpr) context.mkITE(context.mkGt(x, y), y, x);
    }

    private RealExpr avg( ArithExpr x, ArithExpr y) throws Z3Exception {
        RealExpr s = context.mkReal("2.0");
        return (RealExpr) context.mkITE(context.mkGt(x, y), y, x);
    }

    private RealExpr add( RealExpr x, RealExpr y) throws Z3Exception {
        return (RealExpr) context.mkAdd(x,y);
    }
    private RealExpr sub( RealExpr x, RealExpr y) throws Z3Exception {
        return (RealExpr) context.mkSub(x,y);
    }
    private RealExpr mul(RealExpr x, RealExpr y) throws Z3Exception {
        return (RealExpr) context.mkMul(x,y);
    }
    private RealExpr div( RealExpr x, RealExpr y) throws Z3Exception {
        return (RealExpr) context.mkDiv(x,y);
    }
}