package cn.alphabets.light;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.Environment;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.jtwig.environment.EnvironmentFactory;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;
import org.jtwig.resource.reference.ResourceReference;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Helper
 */
public class Helper {

    private static JtwigTemplate template;

    /**
     * Use the template file to generate a string
     *
     * @param name resource name
     * @param model parameters
     * @param function custom functions
     * @return The resulting string
     */
    public static String loadTemplate(String name, Map<String, Object> model, List<TemplateFunction> function) {

        if (template == null) {
            EnvironmentConfiguration configuration = EnvironmentConfigurationBuilder
                    .configuration()
                    .functions()
                    .add(function)
                    .and()
                    .parser()
                    .syntax()
                    .withStartCode("<%~").withEndCode("%>")
                    .withStartOutput("<%=").withEndOutput("%>")
                    .withStartComment("<#").withEndComment("#>")
                    .and()
                    .and()
                    .build();

            Environment environment = new EnvironmentFactory().create(configuration);
            ResourceReference resource = new ResourceReference(ResourceReference.CLASSPATH, name);

            template = new JtwigTemplate(environment, resource);
        }
        return template.render(JtwigModel.newModel(model));
    }

    public static class TemplateFunction extends SimpleJtwigFunction {

        private String name;
        Function<List<Object>, String> function;

        public TemplateFunction(String name, Function<List<Object>, String> function) {
            this.name = name;
            this.function = function;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Object execute(FunctionRequest request) {
            return this.function.apply(request.getArguments());
        }
    }
}
