package br.com.comcet.tp4;

import br.com.comcet.tp1.ast.*;
import br.com.comcet.tp4.parser.MiniPascalBaseVisitor;
import br.com.comcet.tp4.parser.MiniPascalParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyVisitor extends MiniPascalBaseVisitor<AstNode> {

    @Override
    public AstNode visitProgram(MiniPascalParser.ProgramContext ctx) {
        List<Command> commands = new ArrayList<>();

        if (ctx.varDecl() != null) {
            List<MiniPascalParser.IdListContext> idLists = ctx.varDecl().idList();
            List<MiniPascalParser.TypeContext> types = ctx.varDecl().type();
            for (int i = 0; i < idLists.size(); i++) {
                List<String> names = idLists.get(i).ID().stream()
                        .map(id -> id.getText())
                        .collect(Collectors.toList());
                String typeName = types.get(i).getText();
                commands.add(new VarDeclCommand(names, typeName));
            }
        }

        if (ctx.block() != null && ctx.block().commandList() != null) {
            for (MiniPascalParser.CommandContext cmdCtx : ctx.block().commandList().command()) {
                commands.add((Command) visit(cmdCtx));
            }
        }

        return new Program(ctx.ID().getText(), commands);
    }

    @Override
    public AstNode visitAssignment(MiniPascalParser.AssignmentContext ctx) {
        Identifier id = new Identifier(ctx.ID().getText());
        Expression expr = (Expression) visit(ctx.expression());
        return new AssignmentCommand(id, expr);
    }

    @Override
    public AstNode visitIfCommand(MiniPascalParser.IfCommandContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());
        Command thenBranch = (Command) visit(ctx.command(0));
        Command elseBranch = ctx.command().size() > 1
                ? (Command) visit(ctx.command(1))
                : null;
        return new IfCommand(condition, thenBranch, elseBranch);
    }

    @Override
    public AstNode visitWhileCommand(MiniPascalParser.WhileCommandContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());
        Command body = (Command) visit(ctx.command());
        return new WhileCommand(condition, body);
    }

    @Override
    public AstNode visitRepeatCommand(MiniPascalParser.RepeatCommandContext ctx) {
        List<Command> commands = new ArrayList<>();
        for (MiniPascalParser.CommandContext cmdCtx : ctx.commandList().command()) {
            commands.add((Command) visit(cmdCtx));
        }
        Expression condition = (Expression) visit(ctx.expression());
        return new RepeatCommand(new BlockCommand(commands), condition);
    }

    @Override
    public AstNode visitWriteCommand(MiniPascalParser.WriteCommandContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new WritelnCommand(expr);
    }

    @Override
    public AstNode visitReadCommand(MiniPascalParser.ReadCommandContext ctx) {
        Identifier id = new Identifier(ctx.ID().getText());
        return new ReadlnCommand(id);
    }

    @Override
    public AstNode visitBlock(MiniPascalParser.BlockContext ctx) {
        List<Command> commands = new ArrayList<>();
        for (MiniPascalParser.CommandContext cmdCtx : ctx.commandList().command()) {
            commands.add((Command) visit(cmdCtx));
        }
        return new BlockCommand(commands);
    }

    // ── Expressões ────────────────────────────────────────────────────────────

    @Override
    public AstNode visitAddExpr(MiniPascalParser.AddExprContext ctx) {
        Expression left  = (Expression) visit(ctx.expression(0));
        Expression right = (Expression) visit(ctx.expression(1));
        return new BinaryExpression(left, right, ctx.op.getText());
    }

    @Override
    public AstNode visitMulExpr(MiniPascalParser.MulExprContext ctx) {
        Expression left  = (Expression) visit(ctx.expression(0));
        Expression right = (Expression) visit(ctx.expression(1));
        return new BinaryExpression(left, right, ctx.op.getText());
    }

    @Override
    public AstNode visitRelationalExpr(MiniPascalParser.RelationalExprContext ctx) {
        Expression left  = (Expression) visit(ctx.expression(0));
        Expression right = (Expression) visit(ctx.expression(1));
        return new BinaryExpression(left, right, ctx.op.getText());
    }

    @Override
    public AstNode visitLogicalExpr(MiniPascalParser.LogicalExprContext ctx) {
        Expression left  = (Expression) visit(ctx.expression(0));
        Expression right = (Expression) visit(ctx.expression(1));
        return new BinaryExpression(left, right, ctx.op.getText());
    }

    @Override
    public AstNode visitNotExpr(MiniPascalParser.NotExprContext ctx) {
        Expression operand = (Expression) visit(ctx.expression());
        return new UnaryExpression("not", operand);
    }

    @Override
    public AstNode visitUnaryExpr(MiniPascalParser.UnaryExprContext ctx) {
        Expression operand = (Expression) visit(ctx.expression());
        return new UnaryExpression("-", operand);
    }

    @Override
    public AstNode visitParenExpr(MiniPascalParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public AstNode visitNumberExpr(MiniPascalParser.NumberExprContext ctx) {
        return new Literal(ctx.NUMBER().getText());
    }

    @Override
    public AstNode visitStringExpr(MiniPascalParser.StringExprContext ctx) {
        String text = ctx.STRING_LITERAL().getText();
        // Remove as aspas simples
        text = text.substring(1, text.length() - 1);
        return new Literal(text);
    }

    @Override
    public AstNode visitTrueExpr(MiniPascalParser.TrueExprContext ctx) {
        return new Literal("true");
    }

    @Override
    public AstNode visitFalseExpr(MiniPascalParser.FalseExprContext ctx) {
        return new Literal("false");
    }

    @Override
    public AstNode visitIdExpr(MiniPascalParser.IdExprContext ctx) {
        return new Identifier(ctx.ID().getText());
    }
}