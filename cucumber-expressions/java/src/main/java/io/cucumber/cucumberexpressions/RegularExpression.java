package io.cucumber.cucumberexpressions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegularExpression implements Expression {
    private final Pattern expressionRegexp;
    private final ParameterTypeRegistry parameterTypeRegistry;
    private TreeRegexp treeRegexp;

    /**
     * Creates a new instance. Use this when the transform types are not known in advance,
     * and should be determined by the regular expression's capture groups. Use this with
     * dynamically typed languages.
     *  @param expressionRegexp      the regular expression to use
     * @param tableType
     * @param parameterTypeRegistry used to look up parameter types
     */
    public RegularExpression(Pattern expressionRegexp, TableType tableType, ParameterTypeRegistry parameterTypeRegistry) {
        this.expressionRegexp = expressionRegexp;
        this.parameterTypeRegistry = parameterTypeRegistry;
        treeRegexp = new TreeRegexp(expressionRegexp);
    }

    @Override
    public List<Argument<?>> match(String text, DataTable arguments) {
        List<ParameterType<?>> parameterTypes = new ArrayList<>();
        for(GroupBuilder groupBuilder : treeRegexp.getGroupBuilder().getChildren()){
            String parameterTypeRegexp = groupBuilder.getSource();

            ParameterType<?> parameterType = parameterTypeRegistry.lookupByRegexp(parameterTypeRegexp, expressionRegexp, text);
            if (parameterType == null) parameterType = new ParameterType<>(
                    parameterTypeRegexp,
                    parameterTypeRegexp,
                    String.class,
                    new SingleTransformer<>(new Function<String, String>() {
                        @Override
                        public String apply(String s) {
                            return s;
                        }
                    })
            );
            parameterTypes.add(parameterType);
        }

        return ExpressionArgument.build(treeRegexp, parameterTypes, text);
    }


    @Override
    public Pattern getRegexp() {
        return expressionRegexp;
    }

    @Override
    public String getSource() {
        return expressionRegexp.pattern();
    }
}
