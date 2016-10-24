/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.sylvanaar.idea.Lua.lang.parser.kahlua;

import com.intellij.diagnostic.PluginException;
import com.intellij.lang.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.parser.LuaPsiBuilder;
import org.jetbrains.annotations.NotNull;
import se.krka.kahlua.vm.Prototype;

import java.io.IOException;
import java.io.Reader;
import java.util.List;


public class KahluaParser implements PsiParser, LuaElementTypes {

    public int nCcalls = 0;


    static Logger log = Logger.getInstance("Lua.parser.KahluaParser");

    protected static final String RESERVED_LOCAL_VAR_FOR_CONTROL = "(for control)";
    protected static final String RESERVED_LOCAL_VAR_FOR_STATE = "(for state)";
    protected static final String RESERVED_LOCAL_VAR_FOR_GENERATOR = "(for generator)";
    protected static final String RESERVED_LOCAL_VAR_FOR_STEP = "(for step)";
    protected static final String RESERVED_LOCAL_VAR_FOR_LIMIT = "(for limit)";
    protected static final String RESERVED_LOCAL_VAR_FOR_INDEX = "(for index)";

//    // keywords array
//    protected static final String[] RESERVED_LOCAL_VAR_KEYWORDS = new String[]{
//            RESERVED_LOCAL_VAR_FOR_CONTROL,
//            RESERVED_LOCAL_VAR_FOR_GENERATOR,
//            RESERVED_LOCAL_VAR_FOR_INDEX,
//            RESERVED_LOCAL_VAR_FOR_LIMIT,
//            RESERVED_LOCAL_VAR_FOR_STATE,
//            RESERVED_LOCAL_VAR_FOR_STEP
//    };
//    private static final Hashtable<String, Boolean> RESERVED_LOCAL_VAR_KEYWORDS_TABLE =
//            new Hashtable<String, Boolean>();
//
//    static {
//        int i = 0;
//        while (i < RESERVED_LOCAL_VAR_KEYWORDS.length) {
//            String RESERVED_LOCAL_VAR_KEYWORD = RESERVED_LOCAL_VAR_KEYWORDS[i];
//            RESERVED_LOCAL_VAR_KEYWORDS_TABLE.put(RESERVED_LOCAL_VAR_KEYWORD, Boolean.TRUE);
//            i++;
//        }
//    }

    private static final int EOZ = (-1);
    //private static final int MAXSRC = 80;
    private static final int MAX_INT = Integer.MAX_VALUE - 2;
    //private static final int UCHAR_MAX = 255; // TO DO, convert to unicode CHAR_MAX?
    private static final int LUAI_MAXCCALLS = 200;
    private LuaPsiBuilder builder = null;
    boolean checkAmbiguituy = true;


//    public KahluaParser(Project project) {
//    }

    private static String LUA_QS(String s) {
        return "'" + s + "'";
    }

    private static String LUA_QL(Object o) {
        return LUA_QS(String.valueOf(o));
    }

//    public static boolean isReservedKeyword(String varName) {
//        return RESERVED_LOCAL_VAR_KEYWORDS_TABLE.containsKey(varName);
//    }

    /*
     ** Marks the end of a patch list. It is an invalid value both as an absolute
     ** address, and as a list link (would link an element to itself).
     */
    static final int NO_JUMP = (-1);

    /*
     ** grep "ORDER OPR" if you change these enums
     */
    static final int
            OPR_ADD = 0, OPR_SUB = 1, OPR_MUL = 2, OPR_DIV = 3, OPR_MOD = 4, OPR_POW = 5,
            OPR_CONCAT = 6,
            OPR_NE = 7, OPR_EQ = 8,
            OPR_LT = 9, OPR_LE = 10, OPR_GT = 11, OPR_GE = 12,
            OPR_AND = 13, OPR_OR = 14,
            OPR_NOBINOPR = 15;

    static final int
            OPR_MINUS = 0, OPR_NOT = 1, OPR_LEN = 2, OPR_NOUNOPR = 3;

    /* exp kind */
    static final int
            VVOID = 0,    /* no value */
            VNIL = 1,
            VTRUE = 2,
            VFALSE = 3,
            VK = 4,        /* info = index of constant in `k' */
            VKNUM = 5,    /* nval = numerical value */
            VLOCAL = 6,    /* info = local register */
            VUPVAL = 7,       /* info = index of upvalue in `upvalues' */
            VGLOBAL = 8,    /* info = index of table, aux = index of global name in `k' */
            VINDEXED = 9,    /* info = table register, aux = index register (or `k') */
            VJMP = 10,        /* info = instruction pc */
            VRELOCABLE = 11,    /* info = instruction pc */
            VNONRELOC = 12,    /* info = result register */
            VCALL = 13,    /* info = instruction pc */
            VVARARG = 14;    /* info = instruction pc */


    int current = 0;  /* current character (charint) */
    int linenumber = 0;  /* input line counter */
    int lastline = 0;  /* line of last token `consumed' */
    IElementType t = null;  /* current token */
    IElementType lookahead = null;  /* look ahead token */
    FuncState fs = null;  /* `FuncState' is private to the parser */
    Reader z = null;  /* input stream */
    byte[] buff = null;  /* buffer for tokens */
    int nbuff = 0; /* length of buffer */
    String source = null;  /* current source name */

    public KahluaParser(Reader stream, int firstByte, String source) {
        this.z = stream;
        this.buff = new byte[32];
        this.lookahead = null; /* no look-ahead token */
        this.fs = null;
        this.linenumber = 1;
        this.lastline = 1;
        this.source = source;
        this.nbuff = 0;   /* initialize buffer */
        this.current = firstByte; /* read first char */
        this.skipShebang();
    }

    public KahluaParser() {
    }

    ;

    void nextChar() {
        try {
            current = z.read();
        } catch (IOException e) {
            e.printStackTrace();
            current = EOZ;
        }
    }

    boolean currIsNewline() {
        return current == '\n' || current == '\r';
    }

    void lexerror(String msg, IElementType token) {
        String cid = source;
        String errorMessage;
        if (token != null) {
            errorMessage = cid + ":" + linenumber + ": " + msg + " near `" + token + "`";
        } else {
            errorMessage = cid + ":" + linenumber + ": " + msg;
        }

        builder.error(errorMessage);
        //throw new KahluaException(errorMessage);
    }

//    private static String trim(String s, int max) {
//        if (s.length() > max) {
//            return s.substring(0, max - 3) + "...";
//        }
//        return s;
//    }

    void syntaxerror(String msg) {
        lexerror(msg, t);
    }

    private void skipShebang() {
        if (current == '#')
            while (!currIsNewline() && current != EOZ)
                nextChar();
    }


    /*
     ** =======================================================
     ** LEXICAL ANALYZER
     ** =======================================================
     */


    void next() {
        lastline = linenumber;
        builder.advanceLexer();
        t = builder.getTokenType();


//        /*
//		if (lookahead != TK_EOS) { /* is there a look-ahead token? */
//			t.set( lookahead ); /* use this one */
//			lookahead = TK_EOS; /* and discharge it */
//		} else
//			t = llex(t); /* read next token */
//    */
    }

    void lookahead() {
//		FuncState._assert (lookahead == TK_EOS);
        int line = linenumber;
        int last = lastline;
        PsiBuilder.Marker current = builder.mark();
        builder.advanceLexer();
        lookahead = builder.getTokenType();
        current.rollbackTo();
        t = builder.getTokenType();
        linenumber = line;
        lastline = last;
    }

    // =============================================================
    // from lcode.h
    // =============================================================


    // =============================================================
    // from lparser.c
    // =============================================================

    boolean hasmultret(int k) {
        return ((k) == VCALL || (k) == VVARARG);
    }

    /*----------------------------------------------------------------------
     name		args	description
     ------------------------------------------------------------------------*/

    /*
      * * prototypes for recursive non-terminal functions
      */

    void error_expected(IElementType token) {
        syntaxerror(token.toString() + " expected");
    }

    boolean testnext(IElementType c) {
        if (t == c) {
            next();
            return true;
        }

        return false;
    }

    boolean check(IElementType c) {
        final boolean b = t == c;
        if (!b)
            error_expected(c);

        return b;
    }

    void checknext(IElementType c) {
        check(c);
        next();
    }

    void check_condition(boolean c, String msg) {
        if (!(c))
            syntaxerror(msg);
    }


    void check_match(IElementType what, IElementType who, int where) {
        if (!testnext(what)) {
            if (where == linenumber)
                error_expected(what);
            else {
                syntaxerror(what
                        + " expected " + "(to close " + who.toString()
                        + " at line " + where + ")");
            }
        }
    }

    String str_checkname() {
        String ts;

        check(NAME);

        ts = builder.text();

        next();
        return ts;
    }

    void codestring(ExpDesc e, String s) {
        e.init(VK, fs.stringK(s));
    }

    void checkname(ExpDesc e) {
        codestring(e, str_checkname());
    }


    int registerlocalvar(String varname) {
        FuncState fs = this.fs;
        if (fs.locvars == null || fs.nlocvars + 1 > fs.locvars.length)
            fs.locvars = FuncState.realloc(fs.locvars, fs.nlocvars * 2 + 1);
        fs.locvars[fs.nlocvars] = varname;
        return fs.nlocvars++;
    }


//
//	#define new_localvarliteral(ls,v,n) \

    //	  this.new_localvar(luaX_newstring(ls, "" v, (sizeof(v)/sizeof(char))-1), n)
//
    void new_localvarliteral(String v, int n) {
        new_localvar(v, n);
    }

    void new_localvar(String name, int n) {
        FuncState fs = this.fs;
        if (fs.checklimit(fs.nactvar + n + 1, FuncState.LUAI_MAXVARS, "local variables"))
            fs.actvar[fs.nactvar + n] = (short) registerlocalvar(name);
    }

    void adjustlocalvars(int nvars) {
        FuncState fs = this.fs;
        fs.nactvar = (fs.nactvar + nvars);
    }

    void removevars(int tolevel) {
        FuncState fs = this.fs;
        fs.nactvar = tolevel;
    }

    static final int DEC_REF = 0;
    static final int DEC_G = 2;
    static final int DEC_GL = 3;

    void singlevar(ExpDesc var, int statementType) {
        PsiBuilder.Marker ref = builder.mark();

        PsiBuilder.Marker mark = builder.mark();
        String varname = this.str_checkname();

        FuncState fs = this.fs;
        int type = fs.singlevaraux(varname, var, 1);
        switch (type) {

            case VGLOBAL:
                var.info = fs.stringK(varname); /* info points to global name */
                mark.done(statementType >= DEC_G ? GLOBAL_NAME_DECL : GLOBAL_NAME);
                break;

            case VUPVAL:
                mark.done(UPVAL_NAME);
                break;

            case VLOCAL:
                mark.done(statementType == DEC_GL ? LOCAL_NAME_DECL : LOCAL_NAME);
                break;


            default:
                mark.error("Impossible identifier type");
        }

        ref.done(REFERENCE);
    }

    void adjust_assign(int nvars, int nexps, ExpDesc e) {
        FuncState fs = this.fs;
        int extra = nvars - nexps;
        if (hasmultret(e.k)) {
            /* includes call itself */
            extra++;
            if (extra < 0)
                extra = 0;
            /* last exp. provides the difference */
            fs.setreturns(e, extra);
            if (extra > 1)
                fs.reserveregs(extra - 1);
        } else {
            /* close last expression */
            if (e.k != VVOID)
                fs.exp2nextreg(e);
            if (extra > 0) {
                int reg = fs.freereg;
                fs.reserveregs(extra);
                fs.nil(reg, extra);
            }
        }
    }

    void enterlevel() {
        if (++nCcalls > LUAI_MAXCCALLS)
            lexerror("chunk has too many syntax levels", EMPTY_INPUT);
    }

    void leavelevel() {
        nCcalls--;
    }

    void pushclosure(FuncState func, ExpDesc v) {
        FuncState fs = this.fs;
        Prototype f = fs.f;
        if (f.prototypes == null || fs.np + 1 > f.prototypes.length)
            f.prototypes = FuncState.realloc(f.prototypes, fs.np * 2 + 1);
        f.prototypes[fs.np++] = func.f;
        v.init(VRELOCABLE, fs.codeABx(FuncState.OP_CLOSURE, 0, fs.np - 1));
        for (int i = 0; i < func.f.numUpvalues; i++) {
            int o = (func.upvalues_k[i] == VLOCAL) ? FuncState.OP_MOVE
                    : FuncState.OP_GETUPVAL;
            fs.codeABC(o, 0, func.upvalues_info[i], 0);
        }
    }

    void close_func() {
        FuncState fs = this.fs;
        Prototype f = fs.f;
        f.isVararg = fs.isVararg != 0;

        this.removevars(0);
        fs.ret(0, 0); /* final return */
        f.code = FuncState.realloc(f.code, fs.pc);
        f.lines = FuncState.realloc(f.lines, fs.pc);
        // f.sizelineinfo = fs.pc;
        f.constants = FuncState.realloc(f.constants, fs.nk);
        f.prototypes = FuncState.realloc(f.prototypes, fs.np);
        fs.locvars = FuncState.realloc(fs.locvars, fs.nlocvars);
        // f.sizelocvars = fs.nlocvars;
        fs.upvalues = FuncState.realloc(fs.upvalues, f.numUpvalues);
        // FuncState._assert (CheckCode.checkcode(f));
        FuncState._assert(fs.bl == null);
        this.fs = fs.prev;
//		L.top -= 2; /* remove table and prototype from the stack */
        // /* last token read was anchored in defunct function; must reanchor it
        // */
        // if (fs!=null) ls.anchor_token();
    }

    /*============================================================*/
    /* GRAMMAR RULES */
    /*============================================================*/

    void field(ExpDesc v) {
        /* field -> ['.' | ':'] NAME */

        FuncState fs = this.fs;
        ExpDesc key = new ExpDesc();
        fs.exp2anyreg(v);
        this.next(); /* skip the dot or colon */
        PsiBuilder.Marker mark = builder.mark();
        this.checkname(key);
        mark.done(FIELD_NAME);
        //mark.precede().done(REFERENCE);

        fs.indexed(v, key);
    }

    void field_org(ExpDesc v) {
        /* field -> ['.' | ':'] NAME */
        ExpDesc key = new ExpDesc();
        this.next(); /* skip the dot or colon */
        this.checkname(key);
    }

    void yindex(ExpDesc v) {
        /* index -> '[' expr ']' */
        this.next(); /* skip the '[' */
        //   PsiBuilder.Marker mark = builder.mark();
        this.expr(v);
        // mark.done(FIELD_NAME);
        this.fs.exp2val(v);
        this.checknext(RBRACK);
    }

    void yindex_org(ExpDesc v) {
        /* index -> '[' expr ']' */
        this.next(); /* skip the '[' */
        this.expr(v);
        this.checknext(RBRACK);
    }

    /*
     ** {======================================================================
     ** Rules for Constructors
     ** =======================================================================
     */


    void recfield(ConsControl cc) {
        /* recfield -> (NAME | `['exp1`]') = exp1 */
        PsiBuilder.Marker mark = builder.mark();
        PsiBuilder.Marker field = builder.mark();
        FuncState fs = this.fs;
        int reg = this.fs.freereg;
        ExpDesc key = new ExpDesc();
        ExpDesc val = new ExpDesc();
        int rkkey;
        if (this.t == NAME) {
            fs.checklimit(cc.nh, MAX_INT, "items in a constructor");
            this.checkname(key);
            field.done(FIELD_NAME);
        } else {
            field.drop();
            /* this.t == '[' */
            this.yindex(key);
        }
        cc.nh++;
        this.checknext(ASSIGN);
        rkkey = fs.exp2RK(key);
        this.expr(val);
        fs.codeABC(FuncState.OP_SETTABLE, cc.t.info, rkkey, fs.exp2RK(val));
        fs.freereg = reg; /* free registers */
        mark.done(KEY_ASSIGNMENT);
    }

    void listfield(ConsControl cc) {
        PsiBuilder.Marker mark = builder.mark();
        this.expr(cc.v);
        fs.checklimit(cc.na, MAX_INT, "items in a constructor");
        cc.na++;
        cc.tostore++;
        mark.done(IDX_ASSIGNMENT);
    }


    void constructor(ExpDesc t) {
        PsiBuilder.Marker mark = builder.mark();

        /* constructor -> ?? */
        FuncState fs = this.fs;
        int line = this.linenumber;
        int pc = fs.codeABC(FuncState.OP_NEWTABLE, 0, 0, 0);
        ConsControl cc = new ConsControl();
        cc.na = cc.nh = cc.tostore = 0;
        cc.t = t;
        t.init(VRELOCABLE, pc);
        cc.v.init(VVOID, 0); /* no value (yet) */
        fs.exp2nextreg(t); /* fix it at stack top (for gc) */
        this.checknext(LCURLY);

        do {
            FuncState._assert(cc.v.k == VVOID || cc.tostore > 0);
            if (this.t == RCURLY)
                break;
            fs.closelistfield(cc);
            if (this.t == NAME) {

                /* may be listfields or recfields */
                this.lookahead();
                if (this.lookahead != ASSIGN) /* expression? */
                    this.listfield(cc);
                else
                    this.recfield(cc);
                //		break;
            } else if (this.t == LBRACK) { /* constructor_item -> recfield */
                this.recfield(cc);
                //		break;
            } else { /* constructor_part -> listfield */
                this.listfield(cc);
                //		break;
            }

        } while (this.testnext(COMMA) || this.testnext(SEMI));
        this.check_match(RCURLY, LCURLY, line);
        fs.lastlistfield(cc);
        InstructionPtr i = new InstructionPtr(fs.f.code, pc);
        FuncState.SETARG_B(i, luaO_int2fb(cc.na)); /* set initial array size */
        FuncState.SETARG_C(i, luaO_int2fb(cc.nh));  /* set initial table size */

        mark.done(TABLE_CONSTUCTOR);
    }

    /*
     ** converts an integer to a "floating point byte", represented as
     ** (eeeeexxx), where the real value is (1xxx) * 2^(eeeee - 1) if
     ** eeeee != 0 and (xxx) otherwise.
     */
    static int luaO_int2fb(int x) {
        int e = 0;  /* expoent */
        while (x >= 16) {
            x = (x + 1) >> 1;
            e++;
        }
        if (x < 8) return x;
        else return ((e + 1) << 3) | (x - 8);
    }


    /* }====================================================================== */

    void parlist() {
        //log.info(">>> parlist");

        /* parlist -> [ param { `,' param } ] */
        FuncState fs = this.fs;
        Prototype f = fs.f;
        int nparams = 0;
        fs.isVararg = 0;
        if (this.t != RPAREN) {  /* is `parlist' not empty? */

            do {
                PsiBuilder.Marker parm = builder.mark();
//                    PsiBuilder.Marker mark = builder.mark();
                if (this.t == NAME) {
                    /* param . NAME */

                    String name = this.str_checkname();
//                    mark.done(LOCAL_NAME_DECL);
                    this.new_localvar(name, nparams++);
                    parm.done(PARAMETER);
                    // break;
                } else if (this.t == ELLIPSIS) {  /* param . `...' */
                    this.next();
//                    mark.done(LOCAL_NAME_DECL);

                    parm.done(PARAMETER);
                    fs.isVararg |= FuncState.VARARG_ISVARARG;
                    //   break;
                } else {
//                    mark.drop();
                    parm.drop();
                    this.syntaxerror("<name> or " + LUA_QL("...") + " expected");
                }
            } while ((fs.isVararg == 0) && this.testnext(COMMA));

        }
        this.adjustlocalvars(nparams);
        f.numParams = (fs.nactvar - (fs.isVararg & FuncState.VARARG_HASARG));
        fs.reserveregs(fs.nactvar);  /* reserve register for parameters */

        //log.info("<<< parlist");
    }


    void body(ExpDesc e, boolean needself, int line) {
        /* body -> `(' parlist `)' chunk END */
        FuncState new_fs = new FuncState(this);
        new_fs.linedefined = line;
        this.checknext(LPAREN);
        if (needself) {
            PsiBuilder.Marker self = builder.mark();
            new_localvarliteral("self", 0);
            adjustlocalvars(1);
            self.done(SELF_PARAMETER);
        }

        PsiBuilder.Marker mark = builder.mark();
        this.parlist();

        mark.done(LuaElementTypes.PARAMETER_LIST);

        this.checknext(RPAREN);

        mark = builder.mark();
        this.chunk();
        mark.done(BLOCK);
        mark.setCustomEdgeTokenBinders(WhitespacesBinders.DEFAULT_RIGHT_BINDER,TRAILING_WHITESPACE_AND_COMMENT_BINDER);

        closingBlock = true;

        new_fs.lastlinedefined = this.linenumber;
        this.check_match(END, FUNCTION, line);

        this.close_func();
        this.pushclosure(new_fs, e);
    }

    int explist1(ExpDesc v) {
        PsiBuilder.Marker mark = builder.mark();

        /* explist1 -> expr { `,' expr } */
        int n = 1; /* at least one expression */
        this.expr(v);
        while (this.testnext(COMMA)) {
            fs.exp2nextreg(v);
            this.expr(v);
            n++;
        }

        mark.done(EXPR_LIST);
        return n;
    }


    void funcargs(ExpDesc f) {
        PsiBuilder.Marker mark = builder.mark();

        FuncState fs = this.fs;
        ExpDesc args = new ExpDesc();
        int base, nparams;
        int line = this.linenumber;

        if (this.t == LPAREN) { /* funcargs -> `(' [ explist1 ] `)' */
            if (checkAmbiguituy && line != this.lastline)
                this.syntaxerror("ambiguous syntax (function call x new statement)");
            this.next();
            if (this.t == RPAREN) /* arg list is empty? */
                args.k = VVOID;
            else {
                this.explist1(args);
                fs.setmultret(args);
            }
            this.check_match(RPAREN, LPAREN, line);
            //	break;
        } else if (this.t == LCURLY) {
            PsiBuilder.Marker exprlist = builder.mark();
            /* funcargs -> constructor */
            this.constructor(args);

            exprlist.done(EXPR_LIST);

        } else if (this.t == STRING || this.t == LONGSTRING) {  /* funcargs -> STRING */
            this.codestring(args, builder.text());
            PsiBuilder.Marker litstring = builder.mark();
            this.next(); /* must use `seminfo' before `next' */
            litstring.done(LITERAL_EXPRESSION);
            litstring.precede().done(EXPR_LIST);
        } else {
            this.syntaxerror("function arguments expected");

        }

        FuncState._assert(f.k == VNONRELOC);
        base = f.info; /* base register for call */
        if (hasmultret(args.k))
            nparams = FuncState.LUA_MULTRET; /* open call */
        else {
            if (args.k != VVOID)
                fs.exp2nextreg(args); /* close last argument */
            nparams = fs.freereg - (base + 1);
        }
        f.init(VCALL, fs.codeABC(FuncState.OP_CALL, base, nparams + 1, 2));
        fs.fixline(line);
        fs.freereg = base + 1;  /* call remove function and arguments and leaves
                             * (unless changed) one result */


        mark.done(FUNCTION_CALL_ARGS);
    }


    /*
     ** {======================================================================
     ** Expression parsing
     ** =======================================================================
     */

    void prefixexp(ExpDesc v, int statementType) {
        /* prefixexp -> NAME | '(' expr ')' */

        if (this.t == LPAREN) {
            int line = this.linenumber;
            PsiBuilder.Marker mark = builder.mark();
            this.next();
            this.expr(v);
            this.check_match(RPAREN, LPAREN, line);
            mark.done(PARENTHEICAL_EXPRESSION);
            fs.dischargevars(v);
            return;
        } else if (this.t == NAME) {
            this.singlevar(v, statementType);
            return;
        }

        this.syntaxerror("unexpected symbol in prefix expression");
    }


//   void primaryexp(ExpDesc v) {
//		/*
//		 * primaryexp -> prefixexp { `.' NAME | `[' exp `]' | `:' NAME funcargs |
//		 * funcargs }
//		 */
//		FuncState fs = this.fs;
//		this.prefixexp(v);
//		for (;;) {
//			switch (this.t) {
//			case DOT: { /* field */
//				this.field(v);
//				break;
//			}
//			case LBRACK: { /* `[' exp1 `]' */
//				ExpDesc key = new ExpDesc();
//				fs.exp2anyreg(v);
//				this.yindex(key);
//				fs.indexed(v, key);
//				break;
//			}
//			case COLON: { /* `:' NAME funcargs */
//				ExpDesc key = new ExpDesc();
//				this.next();
//				this.checkname(key);
//				fs.self(v, key);
//				this.funcargs(v);
//				break;
//			}
//			case LPAREN:
//			case STRING:
//            case LONGSTRING:
//			case LCURLY: { /* funcargs */
//				fs.exp2nextreg(v);
//				this.funcargs(v);
//				break;
//			}
//			default:
//				return;
//			}
//		}
//	}


    void primaryexp(ExpDesc v, int statementType) {
        /*
           * primaryexp -> prefixexp { `.' NAME | `[' exp `]' | `:' NAME funcargs |
           * funcargs }
           */

        PsiBuilder.Marker mark = builder.mark();

        FuncState fs = this.fs;

        String startName = builder.text();

        this.prefixexp(v, statementType);
        for (; ; ) {
            if (this.t == DOT) { /* field */
                this.field(v);

                mark.done(GETTABLE);
                mark = mark.precede();
                mark.done(COMPOUND_REFERENCE);
                mark = mark.precede();

            } else if (this.t == LBRACK) { /* `[' exp1 `]' */
                ExpDesc key = new ExpDesc();

                fs.exp2anyreg(v);
                this.yindex(key);
                fs.indexed(v, key);

                mark.done(GETTABLE);
                mark = mark.precede();
                mark.done(COMPOUND_REFERENCE);
                mark = mark.precede();

            } else if (this.t == COLON) { /* `:' NAME funcargs */
                ExpDesc key = new ExpDesc();

                this.next();

                PsiBuilder.Marker tmp = builder.mark();
                this.checkname(key);
                tmp.done(FIELD_NAME);

                mark.done(GETTABLE);
                mark = mark.precede();
                mark.done(COMPOUND_REFERENCE);
                mark = mark.precede();

                fs.self(v, key);

//
//                this.funcargs(v);
//                mark.done(FUNCTION_CALL_EXPR);
//
//                mark = mark.precede();
            } else if (this.t == LPAREN
                    || this.t == STRING || this.t == LONGSTRING
                    || this.t == LCURLY) { /* funcargs */

                int type = startName != null ? fs.singlevaraux(startName, v, 1) : KahluaParser.VVOID;

                fs.exp2nextreg(v);

                this.funcargs(v);

                if (startName != null && startName.equals("module") && type == KahluaParser.VGLOBAL)
                    mark.done(MODULE_NAME_DECL);
                else
                    mark.done(FUNCTION_CALL_EXPR);
                mark = mark.precede();

                //break;
            } else {
                mark.drop();
                return;
            }

            startName = null;
        }
    }


    void simpleexp(ExpDesc v) {
        /*
           * simpleexp -> NUMBER | STRING | NIL | true | false | ... | constructor |
           * FUNCTION body | primaryexp
           */

        PsiBuilder.Marker mark = builder.mark();

        try {
            if (this.t == NUMBER) {
                v.init(VKNUM, 0);
                v.setNval(0); // TODO
            } else if (this.t == STRING || this.t == LONGSTRING) {

                this.codestring(v, builder.text()); //TODO

            } else if (this.t == NIL) {
                v.init(VNIL, 0);
            } else if (this.t == TRUE) {
                v.init(VTRUE, 0);
            } else if (this.t == FALSE) {
                v.init(VFALSE, 0);
            } else if (this.t == ELLIPSIS) { /* vararg */
                FuncState fs = this.fs;
                this.check_condition(fs.isVararg != 0, "cannot use " + LUA_QL("...")
                        + " outside a vararg function");
                fs.isVararg &= ~FuncState.VARARG_NEEDSARG; /* don't need 'arg' */
                v.init(VVARARG, fs.codeABC(FuncState.OP_VARARG, 0, 1, 0));


                PsiBuilder.Marker ref = mark.precede();
                this.next();

                mark.done(LOCAL_NAME);
                ref.done(REFERENCE);
                mark = null;
                return;
            } else if (this.t == LCURLY) { /* constructor */
                this.constructor(v);
                return;
            } else if (this.t == FUNCTION) {
                PsiBuilder.Marker funcStmt = builder.mark();
                this.next();
                this.body(v, false, this.linenumber);
                funcStmt.done(ANONYMOUS_FUNCTION_EXPRESSION);
                return;
            } else {
                this.primaryexp(v, DEC_REF);
                return;
            }
            this.next();

            mark.done(LITERAL_EXPRESSION);
            mark = null;
        } finally {
            if (mark != null)
                mark.drop();
        }
    }


    int getunopr(IElementType op) {
        if (op == NOT)
            return OPR_NOT;
        if (op == MINUS)
            return OPR_MINUS;
        if (op == GETN)
            return OPR_LEN;

        return OPR_NOUNOPR;
    }


    int getbinopr(IElementType op) {

        if (op == PLUS)
            return OPR_ADD;
        if (op == MINUS)
            return OPR_SUB;
        if (op == MULT)
            return OPR_MUL;
        if (op == DIV)
            return OPR_DIV;
        if (op == MOD)
            return OPR_MOD;
        if (op == EXP)
            return OPR_POW;
        if (op == CONCAT)
            return OPR_CONCAT;
        if (op == NE)
            return OPR_NE;
        if (op == EQ)
            return OPR_EQ;
        if (op == LT)
            return OPR_LT;
        if (op == LE)
            return OPR_LE;
        if (op == GT)
            return OPR_GT;
        if (op == GE)
            return OPR_GE;
        if (op == AND)
            return OPR_AND;
        if (op == OR)
            return OPR_OR;

        return OPR_NOBINOPR;
    }

    static final int[] priorityLeft = {
            6, 6, 7, 7, 7,  /* `+' `-' `/' `%' */
            10, 5,                 /* power and concat (right associative) */
            3, 3,                  /* equality and inequality */
            3, 3, 3, 3,  /* order */
            2, 1,                    /* logical (and/or) */
    };

    static final int[] priorityRight = {  /* ORDER OPR */
            6, 6, 7, 7, 7,  /* `+' `-' `/' `%' */
            9, 4,                 /* power and concat (right associative) */
            3, 3,                  /* equality and inequality */
            3, 3, 3, 3,  /* order */
            2, 1                   /* logical (and/or) */
    };

    static final int UNARY_PRIORITY = 8;  /* priority for unary operators */


    /*
     ** subexpr -> (simpleexp | unop subexpr) { binop subexpr }
     ** where `binop' is any binary operator with a priority higher than `limit'
     */
    int subexpr(ExpDesc v, int limit) {
        int op;
        int uop;
        PsiBuilder.Marker mark = builder.mark();
        PsiBuilder.Marker oper;

        this.enterlevel();
        uop = getunopr(this.t);
        if (uop != OPR_NOUNOPR) {
            PsiBuilder.Marker mark2 = builder.mark();
            oper = builder.mark();
            this.next();
            oper.done(UNARY_OP);

            this.subexpr(v, UNARY_PRIORITY);
            mark2.done(UNARY_EXP);
            fs.prefix(uop, v);
        } else {
            this.simpleexp(v);

        }

        /* expand while operators have priorities higher than `limit' */
        op = getbinopr(this.t);


        while (op != OPR_NOBINOPR && priorityLeft[op] > limit) {

            ExpDesc v2 = new ExpDesc();
            int nextop;
            oper = builder.mark();
            this.next();
            oper.done(BINARY_OP);
            fs.infix(op, v);



            /* read sub-expression with higher priority */
            nextop = this.subexpr(v2, priorityRight[op]);
            fs.posfix(op, v, v2);
            op = nextop;

            mark.done(BINARY_EXP);
            mark = mark.precede();
        }

        mark.drop();

        this.leavelevel();
        return op; /* return first untreated operator */
    }

    void expr(ExpDesc v) {
        // PsiBuilder.Marker mark = builder.mark();
        this.subexpr(v, 0);
        // mark.done(EXPR);
        // next();
    }

    /* }==================================================================== */


    /*
     ** {======================================================================
     ** Rules for Statements
     ** =======================================================================
     */


    boolean block_follow(IElementType token) {
        return token == ELSE || token == ELSEIF ||
                token == END || token == UNTIL || token == null;
    }


    void block() {
        PsiBuilder.Marker mark = builder.mark();

        /* block -> chunk */
        FuncState fs = this.fs;
        BlockCnt bl = new BlockCnt();
        fs.enterblock(bl, false);
        this.chunk();
        FuncState._assert(bl.breaklist == NO_JUMP);
        fs.leaveblock();
        mark.done(BLOCK);
        mark.setCustomEdgeTokenBinders(WhitespacesBinders.DEFAULT_RIGHT_BINDER, TRAILING_WHITESPACE_AND_COMMENT_BINDER);
    }

    /*
     ** check whether, in an assignment to a local variable, the local variable
     ** is needed in a previous assignment (to a table). If so, save original
     ** local value in a safe place and use this safe copy in the previous
     ** assignment.
     */
    void check_conflict(LHS_assign lh, ExpDesc v) {
        FuncState fs = this.fs;
        int extra = fs.freereg;  /* eventual position to save local variable */
        boolean conflict = false;
        for (; lh != null; lh = lh.prev) {
            if (lh.v.k == VINDEXED) {
                if (lh.v.info == v.info) {  /* conflict? */
                    conflict = true;
                    lh.v.info = extra;  /* previous assignment will use safe copy */
                }
                if (lh.v.aux == v.info) {  /* conflict? */
                    conflict = true;
                    lh.v.aux = extra;  /* previous assignment will use safe copy */
                }
            }
        }
        if (conflict) {
            fs.codeABC(FuncState.OP_MOVE, fs.freereg, v.info, 0); /* make copy */
            fs.reserveregs(1);
        }
    }


    boolean assignment(LHS_assign lh, int nvars, PsiBuilder.Marker expr) {
        // PsiBuilder.Marker mark = builder.mark();
        ExpDesc e = new ExpDesc();
        this.check_condition(VLOCAL <= lh.v.k && lh.v.k <= VINDEXED,
                "syntax error");
        if (this.testnext(COMMA)) {  /* assignment -> `,' primaryexp assignment */
            LHS_assign nv = new LHS_assign();
            nv.prev = lh;

            lookahead();
            boolean def = lookahead != DOT && lookahead != LBRACK;
            this.primaryexp(nv.v, def ? DEC_G : DEC_REF);

            if (nv.v.k == VLOCAL)
                this.check_conflict(lh, nv.v);

            return this.assignment(nv, nvars + 1, expr);  // recurse with an additional variable
        } else {  /* assignment . `=' explist1 */

            int nexps;

            expr.done(IDENTIFIER_LIST);
            if (check(ASSIGN)) {
                next();
                nexps = this.explist1(e);
                if (nexps != nvars) {
                    this.adjust_assign(nvars, nexps, e);
                    if (nexps > nvars)
                        this.fs.freereg -= nexps - nvars;  /* remove extra values */
                } else {
                    fs.setoneret(e);  /* close last expression */
                    fs.storevar(lh.v, e);


                    return true;  /* avoid default */
                }
            } else return false;
        }
        e.init(VNONRELOC, this.fs.freereg - 1);  /* default assignment */
        fs.storevar(lh.v, e);
        // mark.done(ASSIGN_STMT);

        return true;
    }


    int cond() {
        PsiBuilder.Marker mark = builder.mark();
        /* cond -> exp */
        ExpDesc v = new ExpDesc();
        /* read condition */
        this.expr(v);
        /* `falses' are all equal here */
        if (v.k == VNIL)
            v.k = VFALSE;
        fs.goiftrue(v);
        mark.done(CONDITIONAL_EXPR);
        return v.f;

    }


    void breakstat() {
        PsiBuilder.Marker mark = builder.mark();

        FuncState fs = this.fs;
        BlockCnt bl = fs.bl;
        boolean upval = false;
        while (bl != null && !bl.isbreakable) {
            upval |= bl.upval;
            bl = bl.previous;
        }
        if (bl == null) {
            this.syntaxerror("no loop to break");
        } else {
            if (upval)
                fs.codeABC(FuncState.OP_CLOSE, bl.nactvar, 0, 0);
            bl.breaklist = fs.concat(bl.breaklist, fs.jump());
        }

        mark.done(BREAK);
    }


    void whilestat(int line) {
        PsiBuilder.Marker mark = builder.mark();

        /* whilestat -> WHILE cond DO block END */
        FuncState fs = this.fs;
        int whileinit;
        int condexit;
        BlockCnt bl = new BlockCnt();
        this.next();  /* skip WHILE */
        whileinit = fs.getlabel();
        condexit = this.cond();
        fs.enterblock(bl, true);
        this.checknext(DO);
        this.block();
        fs.patchlist(fs.jump(), whileinit);
        this.check_match(END, WHILE, line);
        fs.leaveblock();
        fs.patchtohere(condexit);  /* false conditions finish the loop */

        mark.done(WHILE_BLOCK);
    }

    void repeatstat(int line) {
        PsiBuilder.Marker mark = builder.mark();

        /* repeatstat -> REPEAT block UNTIL cond */
        int condexit;
        FuncState fs = this.fs;
        int repeat_init = fs.getlabel();
        BlockCnt bl1 = new BlockCnt();
        BlockCnt bl2 = new BlockCnt();
        fs.enterblock(bl1, true); /* loop block */
        fs.enterblock(bl2, false); /* scope block */
        this.next(); /* skip REPEAT */
        this.chunk();

        PsiBuilder.Marker clause = builder.mark();
        this.check_match(UNTIL, REPEAT, line);
        condexit = this.cond(); /* read condition (inside scope block) */
        clause.done(UNTIL_CLAUSE);
        if (!bl2.upval) { /* no upvalues? */
            fs.leaveblock(); /* finish scope */
            fs.patchlist(condexit, repeat_init); /* close the loop */
        } else { /* complete semantics when there are upvalues */
            this.breakstat(); /* if condition then break */
            fs.patchtohere(condexit); /* else... */
            fs.leaveblock(); /* finish scope... */
            fs.patchlist(fs.jump(), repeat_init); /* and repeat */
        }
        fs.leaveblock(); /* finish loop */

        mark.done(REPEAT_BLOCK);
    }


    int exp1() {
        ExpDesc e = new ExpDesc();
        int k;
        this.expr(e);
        k = e.k;
        fs.exp2nextreg(e);
        return k;
    }


    void forbody(int base, int line, int nvars, boolean isnum) {
        /* forbody -> DO block */
        BlockCnt bl = new BlockCnt();
        FuncState fs = this.fs;
        int prep, endfor;
        this.adjustlocalvars(3); /* control variables */
        this.checknext(DO);
        prep = isnum ? fs.codeAsBx(FuncState.OP_FORPREP, base, NO_JUMP) : fs.jump();
        fs.enterblock(bl, false); /* scope for declared variables */
        this.adjustlocalvars(nvars);
        fs.reserveregs(nvars);
        this.block();
        fs.leaveblock(); /* end of scope for declared variables */
        fs.patchtohere(prep);
        endfor = (isnum) ? fs.codeAsBx(FuncState.OP_FORLOOP, base, NO_JUMP) : fs
                .codeABC(FuncState.OP_TFORLOOP, base, 0, nvars);
        fs.fixline(line); /* pretend that `Lua.OP_FOR' starts the loop */
        fs.patchlist((isnum ? endfor : fs.jump()), prep + 1);
    }


    void fornum(String varname, int line) {
        /* fornum -> NAME = exp1,exp1[,exp1] forbody */
        FuncState fs = this.fs;
        int base = fs.freereg;
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_INDEX, 0);
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_LIMIT, 1);
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_STEP, 2);
        // PsiBuilder.Marker mark = builder.mark();
        this.new_localvar(varname, 3);
        // mark.done(LOCAL_NAME_DECL);
        this.checknext(ASSIGN);
        this.exp1(); /* initial value */
        this.checknext(COMMA);
        this.exp1(); /* limit */
        if (this.testnext(COMMA)) {
            this.exp1(); /* optional step */
        } else { /* default step = 1 */
            fs.codeABx(FuncState.OP_LOADK, fs.freereg, fs.numberK(1));
            fs.reserveregs(1);
        }
        this.forbody(base, line, 1, true);
    }


    void forlist(String indexname) {
        /* forlist -> NAME {,NAME} IN explist1 forbody */
        FuncState fs = this.fs;
        ExpDesc e = new ExpDesc();
        int nvars = 0;
        int line;
        int base = fs.freereg;
        /* create control variables */
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_GENERATOR, nvars++);
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_STATE, nvars++);
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_CONTROL, nvars++);
        /* create declared variables */

        this.new_localvar(indexname, nvars++);

        // next();

        while (this.testnext(COMMA)) {
            PsiBuilder.Marker mark = builder.mark();
            String name = this.str_checkname();
            mark.done(LOCAL_NAME_DECL);
            this.new_localvar(name, nvars++);
        }


        this.checknext(IN);
        line = this.linenumber;
        this.adjust_assign(3, this.explist1(e), e);
        fs.checkstack(3); /* extra space to call generator */
        this.forbody(base, line, nvars - 3, false);
    }


    void forstat(int line) {
        /* forstat -> FOR (fornum | forlist) END */
        FuncState fs = this.fs;
        String varname;
        BlockCnt bl = new BlockCnt();
        fs.enterblock(bl, true); /* scope for loop and control variables */

        PsiBuilder.Marker mark = builder.mark();
        boolean numeric = false;

        this.checknext(FOR); /* skip `for' */

        PsiBuilder.Marker var_mark = builder.mark();
        varname = this.str_checkname(); /* first variable name */
        var_mark.done(LOCAL_NAME_DECL);

        if (this.t == ASSIGN) {
            numeric = true;
            this.fornum(varname, line);
        } else if (this.t == COMMA || this.t == IN) {
            this.forlist(varname);
        } else {
            this.syntaxerror(LUA_QL("=") + " or " + LUA_QL("in") + " expected");
        }
        this.check_match(END, FOR, line);

        mark.done(numeric ? NUMERIC_FOR_BLOCK : GENERIC_FOR_BLOCK);

        fs.leaveblock(); /* loop scope (`break' jumps to this point) */
    }


    int test_then_block() {
        /* test_then_block -> [IF | ELSEIF] cond THEN block */
        int condexit;
        this.next(); /* skip IF or ELSEIF */
        condexit = this.cond();
        this.checknext(THEN);
        this.block(); /* `then' part */
        return condexit;
    }


    void ifstat(int line) {
        PsiBuilder.Marker mark = builder.mark();

        /* ifstat -> IF cond THEN block {ELSEIF cond THEN block} [ELSE block]
           * END */
        FuncState fs = this.fs;
        int flist;
        int escapelist = NO_JUMP;
        flist = test_then_block(); /* IF cond THEN block */
        while (this.t == ELSEIF) {
            escapelist = fs.concat(escapelist, fs.jump());
            fs.patchtohere(flist);
            flist = test_then_block(); /* ELSEIF cond THEN block */
        }
        if (this.t == ELSE) {
            escapelist = fs.concat(escapelist, fs.jump());
            fs.patchtohere(flist);
            this.next(); /* skip ELSE (after patch, for correct line info) */
            this.block(); /* `else' part */
        } else
            escapelist = fs.concat(escapelist, flist);
        fs.patchtohere(escapelist);
        this.check_match(END, IF, line);

        mark.done(IF_THEN_BLOCK);
    }


    // need to parse the same as local foo; function foo() end
    void localfunc(PsiBuilder.Marker stat) {
        ExpDesc v = new ExpDesc();
        ExpDesc b = new ExpDesc();
        FuncState fs = this.fs;

        PsiBuilder.Marker funcStmt = stat;

        next();

        //PsiBuilder.Marker funcName = builder.mark();
        PsiBuilder.Marker mark = builder.mark();
        String name = this.str_checkname();
        mark.done(LOCAL_NAME_DECL);
        // funcName.done(FUNCTION_IDENTIFIER);

        this.new_localvar(name, 0);
        v.init(VLOCAL, fs.freereg);
        fs.reserveregs(1);
        this.adjustlocalvars(1);

        this.body(b, false, this.linenumber);
        funcStmt.done(LOCAL_FUNCTION);
        fs.storevar(v, b);
        /* debug information will only see the variable after this point! */


    }


    void localstat(PsiBuilder.Marker stat) {

        // PsiBuilder.Marker mark = stat;
        PsiBuilder.Marker names = builder.mark();

        /* stat -> LOCAL NAME {`,' NAME} [`=' explist1] */
        int nvars = 0;
        int nexps;
        ExpDesc e = new ExpDesc();
        do {
            PsiBuilder.Marker mark = builder.mark();
            String name = this.str_checkname();
            mark.done(LOCAL_NAME_DECL);
            this.new_localvar(name, nvars++);

        } while (this.testnext(COMMA));

        names.done(IDENTIFIER_LIST);
        if (this.testnext(ASSIGN)) {
            nexps = this.explist1(e);
            stat.done(LOCAL_DECL_WITH_ASSIGNMENT);
        } else {
            e.k = VVOID;
            nexps = 0;
            stat.done(LOCAL_DECL);
        }

        this.adjust_assign(nvars, nexps, e);
        this.adjustlocalvars(nvars);
    }


    boolean funcname(ExpDesc v) {
        //log.info(">>> funcname");
        /* funcname -> NAME {field} [`:' NAME] */
        boolean needself = false;

        lookahead();
        boolean def = lookahead != DOT && lookahead != COLON;

        PsiBuilder.Marker tmp = builder.mark();

        this.singlevar(v, def ? DEC_GL : DEC_REF);

        // OK this should work like    GETTABLE( REF(a) ID(b) )
        while (this.t == DOT) {
            this.field(v);
            tmp.done(GETTABLE);
            tmp = tmp.precede();
            tmp.done(COMPOUND_REFERENCE);
            tmp = tmp.precede();
        }
        if (this.t == COLON) {
            needself = true;

            this.field(v);
            // tmp.done(GETSELF);
            tmp.done(GETTABLE);
            tmp = tmp.precede();
            tmp.done(COMPOUND_REFERENCE);
            tmp = tmp.precede();
        }

        tmp.drop();

        return needself;
    }


    void funcstat(int line) {
        //log.info(">>> funcstat");

        PsiBuilder.Marker funcStmt = builder.mark();

        /* funcstat -> FUNCTION funcname body */
        boolean needself;
        ExpDesc v = new ExpDesc();
        ExpDesc b = new ExpDesc();
        this.next(); /* skip FUNCTION */

        needself = this.funcname(v);

        this.body(b, needself, line);

        funcStmt.done(FUNCTION_DEFINITION);

        fs.storevar(v, b);
        fs.fixline(line); /* definition `happens' in the first line */

        ///log.info("<<< funcstat");
    }


    void funcargs_org(ExpDesc f) {
        PsiBuilder.Marker mark = builder.mark();

        FuncState fs = this.fs;
        ExpDesc args = new ExpDesc();
        int base, nparams;
        int line = this.linenumber;

        if (this.t == LPAREN) { /* funcargs -> `(' [ explist1 ] `)' */
//            if (line != this.lastline)
//                this.syntaxerror("ambiguous syntax (function call x new statement)");
            this.next();
            if (this.t == RPAREN) /* arg list is empty? */
                args.k = VVOID;
            else {
                this.explist1(args);
//                fs.setmultret(args);
            }
            this.check_match(RPAREN, LPAREN, line);
            //	break;
        } else if (this.t == LCURLY) {
            /* funcargs -> constructor */
            this.constructor(args);

        } else if (this.t == STRING || this.t == LONGSTRING) {  /* funcargs -> STRING */
//            this.codestring(args, builder.text());
            this.next(); /* must use `seminfo' before `next' */

        } else {
            this.syntaxerror("function arguments expected");

        }
    }

    private static final short PRI_CALL = 0x0001;
    private static final short PRI_COMP = 0x0002;

    short primaryexp_org(ExpDesc v) {
        boolean isfunc = false;
        boolean isCompound = false;
		/*
		 * primaryexp -> prefixexp { `.' NAME | `[' exp `]' | `:' NAME funcargs |
		 * funcargs }
		 */
        FuncState fs = this.fs;
        this.prefixexp(v, DEC_REF);
        for (; ; ) {
            if (this.t == DOT) { /* field */
                this.field_org(v);
                isfunc = false;
                isCompound = true;
            } else if (this.t == LBRACK) { /* `[' exp1 `]' */
                ExpDesc key = new ExpDesc();
                this.yindex_org(key);
                fs.indexed(v, key);
                isfunc = false;
                isCompound = true;
            } else if (this.t == COLON) { /* `:' NAME funcargs */
                ExpDesc key = new ExpDesc();
                this.next();
                this.checkname(key);
                fs.self(v, key);
                this.funcargs_org(v);
                isfunc = true;
                isCompound = true;
            } else if (this.t == LPAREN
                    || this.t == STRING || this.t == LONGSTRING
                    || this.t == LCURLY) { /* funcargs */
                this.funcargs_org(v);
                isfunc = true;
            } else {
                short rc = isfunc ? PRI_CALL : 0;
                rc |= isCompound ? PRI_COMP : 0;

                return rc;
            }

        }
    }


    void exprstat() {
        /* stat -> func | assignment */
        FuncState fs = this.fs;

        /* DANGER - because of this, this parser will produce invalid bytecode */
        /* this is here so we can know which rule we are actually processing */
        /* because unlike the lua parser, we need to know in advance */
        LHS_assign v = new LHS_assign();
        PsiBuilder.Marker lookahead = builder.mark();
        int savelast = lastline;
        int saveline = linenumber;
        short info = primaryexp_org(v.v);
        boolean isassign = (info & PRI_CALL) == 0;
        boolean isCompound = (info & PRI_COMP) != 0;
        boolean isComplete = !isassign;
        if (isassign) {// need to see if it is a complete assignment statement
            while (t != ASSIGN && !builder.eof() && !LuaTokenTypes.KEYWORDS.contains(t))
                next();

            if (t == ASSIGN)
                isComplete = true;
        }
        lookahead.rollbackTo();
        lastline = savelast;
        linenumber = saveline;
        this.t = builder.getTokenType();

        v = new LHS_assign();

        PsiBuilder.Marker outer = builder.mark();

        this.primaryexp(v.v, (isassign && !isCompound && isComplete) ? DEC_G : DEC_REF);

        if (v.v.k == VCALL) /* stat -> func */ {
            if (isassign)
                builder.error("invalid assign prediction (is call)");


            outer.done(FUNCTION_CALL);
            FuncState.SETARG_C(fs.getcodePtr(v.v), 1); /* call statement uses no results */
        } else { /* stat -> assignment */
            if (!isassign)
                builder.error("invalid call prediction (is assignment)");

            v.prev = null;

            final boolean assignment = this.assignment(v, 1, outer);
            outer.precede().done(ASSIGN_STMT);
            if (!assignment) next();
        }
    }


    boolean closingBlock = false;

    void retstat() {


        PsiBuilder.Marker mark = builder.mark();
        boolean tailCall = false;
        /* stat -> RETURN explist */
        FuncState fs = this.fs;
        ExpDesc e = new ExpDesc();
        int first, nret; /* registers with returned values */
        this.next(); /* skip RETURN */
        if (block_follow(this.t) || this.t == SEMI)
            first = nret = 0; /* return no values */
        else {
            nret = this.explist1(e); /* optional return values */
            if (hasmultret(e.k)) {
                fs.setmultret(e);
                if (e.k == VCALL && nret == 1) { /* tail call? */
                    tailCall = true;
                    FuncState.SET_OPCODE(fs.getcodePtr(e), FuncState.OP_TAILCALL);
                    FuncState._assert(FuncState.GETARG_A(fs.getcode(e)) == fs.nactvar);
                }
                first = fs.nactvar;
                nret = FuncState.LUA_MULTRET; /* return all values */
            } else {
                if (nret == 1) /* only one single value? */
                    first = fs.exp2anyreg(e);
                else {
                    fs.exp2nextreg(e); /* values must go to the `stack' */
                    first = fs.nactvar; /* return all `active' values */
                    FuncState._assert(nret == fs.freereg - first);
                }
            }
        }

        mark.done(tailCall ? RETURN_STATEMENT_WITH_TAIL_CALL : RETURN_STATEMENT);
        closingBlock = true;
        fs.ret(first, nret);
    }


    boolean statement() {

        try {
            //log.info(">>> statement");
            int line = this.linenumber; /* may be needed for error messages */

            if (this.t == IF) { /* stat -> ifstat */
                this.ifstat(line);
                return false;
            }
            if (this.t == WHILE) { /* stat -> whilestat */
                this.whilestat(line);
                return false;
            }
            if (this.t == DO) { /* stat -> DO block END */
                PsiBuilder.Marker mark = builder.mark();
                this.next(); /* skip DO */
                this.block();
                this.check_match(END, DO, line);
                mark.done(DO_BLOCK);
                return false;
            }
            if (this.t == FOR) { /* stat -> forstat */
                this.forstat(line);
                return false;
            }
            if (this.t == REPEAT) { /* stat -> repeatstat */
                this.repeatstat(line);
                return false;
            }
            if (this.t == FUNCTION) {
                this.funcstat(line); /* stat -> funcstat */
                return false;
            }
            if (this.t == LOCAL) { /* stat -> localstat */
                PsiBuilder.Marker stat = builder.mark();
                this.next(); /* skip LOCAL */
                if (this.t == FUNCTION) /* local function? */
                    this.localfunc(stat);
                else
                    this.localstat(stat);
                return false;
            }
            if (this.t == RETURN) { /* stat -> retstat */
                this.retstat();
                return true; /* must be last statement */
            }
            if (this.t == BREAK) { /* stat -> breakstat */
                this.next(); /* skip BREAK */
                this.breakstat();
                return true; /* must be last statement */
            }

            this.exprstat();
            return false; /* to avoid warnings */
        } finally {
            //log.info("<<< statement");
        }
    }

    void chunk() {
        //log.info(">>> chunk");
        /* chunk -> { stat [`;'] } */
        boolean islast = false;
        this.enterlevel();
        while (!islast && !block_follow(this.t)) {
//            final PsiBuilder.Marker mark = builder.mark();
            islast = this.statement();
            this.testnext(SEMI);
//            PsiBuilder.Marker mark = builder.mark();
//            if (this.testnext(SEMI))
//                mark.done(LUA_TOKEN);
//            else
//                mark.drop();

            FuncState._assert(this.fs.f.maxStacksize >= this.fs.freereg
                    && this.fs.freereg >= this.fs.nactvar);
            this.fs.freereg = this.fs.nactvar; /* free registers */
        }


        this.leavelevel();
//        if (builder.isError() || closingBlock)
//            cleanAfterError(builder);

        //log.info("<<< chunk");

    }

//    private void cleanAfterError(LuaPsiBuilder builder) {
//        int i = 0;
//        PsiBuilder.Marker em = builder.mark();
//        while (!builder.eof() && !(END.equals(builder.getTokenType())) ) {
//            builder.advanceLexer();
//            i++;
//        }
//        if (i > 0) {
//            builder.advanceLexer();
//            em.error("Attempting to recover from previous errors.");
//            builder.setError(false);
//        } else {
//            em.rollbackTo();
//            if (closingBlock)
//           closingBlock = false;
//        }
//    }


    /* }====================================================================== */


    @NotNull
    @Override
    public ASTNode parse(IElementType root, PsiBuilder builder) {

        final LuaPsiBuilder psiBuilder = new LuaPsiBuilder(builder);

        try {
            final PsiBuilder.Marker rootMarker = psiBuilder.mark();

            final PsiFile psiFile = psiBuilder.getFile();
            final VirtualFile virtualFile = psiFile != null ? psiFile.getVirtualFile() : null;
            final String name = virtualFile != null ? virtualFile.getName() : "chunk";
            source = name;
            KahluaParser lexstate = new KahluaParser(z, 0, source);

            lexstate.checkAmbiguituy = false;
            lexstate.builder = psiBuilder;
            psiBuilder.setWhitespaceSkippedCallback(lexstate.new ParserWhitespaceSkippedCallback());

            FuncState funcstate = new FuncState(lexstate);
            // lexstate.buff = buff;

            /* main func. is always vararg */
            funcstate.isVararg = FuncState.VARARG_ISVARARG;
            funcstate.f.name = name;


            psiBuilder.mark().done(LuaElementTypes.MAIN_CHUNK_VARARGS);


            lexstate.t = psiBuilder.getTokenType();

            if (log.isDebugEnabled())
                lexstate.builder.debug();
            lexstate.chunk();

            int pos = psiBuilder.getCurrentOffset();
            PsiBuilder.Marker mark = psiBuilder.mark();
            while (!psiBuilder.eof())
                psiBuilder.advanceLexer();

            if (psiBuilder.getCurrentOffset() > pos)
                mark.error("Unparsed code");
            else
                mark.drop();

            // lexstate.check(EMPTY_INPUT);
            lexstate.close_func();

            FuncState._assert(funcstate.prev == null);
            FuncState._assert(funcstate.f.numUpvalues == 0);
            FuncState._assert(lexstate.fs == null);


            //  return funcstate.f;


            if (root != null)
                rootMarker.done(root);
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (Exception e) {
            throw new PluginException("Exception During Parse At Offset: " + builder.getCurrentOffset() + "\n\n" + builder.getOriginalText(), e, PluginId.getId("Lua"));
        }

        return builder.getTreeBuilt();
    }

    private class ParserWhitespaceSkippedCallback implements WhitespaceSkippedCallback {
        @Override
        public void onSkip(IElementType iElementType, int i, int i1) {

            final CharSequence text = KahluaParser.this.builder.getOriginalText();

            KahluaParser.this.linenumber += countLineBreaks(text, i, i1);
        }
    }

    public static int countLineBreaks(CharSequence seq, int fromOffset, int endOffset) {
        if (seq == null) return 0;
        char last = '\0';
        int count = 0;

        for (int i = fromOffset; i < endOffset; i++) {
            final char c = seq.charAt(i);
            if (c == '\n' || c == '\r') {
                if (last == '\0')
                    last = c;

                if (last == c)
                    count++;
            }
        }
        return count;
    }

    private static WhitespacesAndCommentsBinder TRAILING_WHITESPACE_AND_COMMENT_BINDER = new WhitespacesAndCommentsBinder() {
        @Override
        public int getEdgePosition(final List<IElementType> tokens, final boolean atStreamEdge, final WhitespacesAndCommentsBinder.TokenTextGetter getter) {
            if (tokens.size() == 0) return 0;

            int result = 0;
            for (final IElementType tokenType : tokens) {
                if (LuaElementTypes.WHITE_SPACES_OR_COMMENTS.contains(tokenType))
                    result++;
                else
                    break;
            }

            return result;
        }
    };

}