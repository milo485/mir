package mir.generator;

import java.util.HashMap;
import java.util.Map;

import mir.log.LoggerWrapper;
import mir.util.SimpleParser;

public class GeneratorLibraryRepository {
  private Map factories;
  private LoggerWrapper logger;

  public GeneratorLibraryRepository() {
    factories = new HashMap();
    logger = new LoggerWrapper("TemplateEngine");
  }

  public void registerLibraryFactory(String aName, Generator.GeneratorLibraryFactory aFactory) {
    factories.put(aName, aFactory);
  }

  public Generator.GeneratorLibrary constructLibrary(String aName, String aParameters) throws GeneratorExc {
    if (!factories.containsKey(aName))
      throw new GeneratorExc("Unknown library factory: "+aName);

    return ((Generator.GeneratorLibraryFactory) factories.get(aName)).makeLibrary(aParameters);
  }

  private final static String SPACE = "[\t\n\r ]*";
  private final static String IDENTIFIER = "[a-zA-Z_][a-zA-Z0-9_]*";
  private final static String EQUALS = "=";
  private final static String LEFT_PARENTHESIS = "[(]";
  private final static String RIGHT_PARENTHESIS = "[)]";
  private final static String FACTORY_PARAMETERS = "[^)]*";
  private final static String SEMICOLON = ";";

  public Generator.GeneratorLibrary constructCompositeLibrary(String aSpecification) throws GeneratorExc, GeneratorFailure {
    String identifier;
    String factory;
    String factoryParameters;
    CompositeGeneratorLibrary result = new CompositeGeneratorLibrary();
    boolean first=true;

    SimpleParser parser = new SimpleParser(aSpecification);
    try {
      parser.skip(SPACE);
      while (!parser.isAtEnd()) {
        identifier = parser.parse(IDENTIFIER, "library key expected");
        parser.skip(SPACE);
        parser.parse(EQUALS, "'=' expected");
        parser.skip(SPACE);
        factory = parser.parse(IDENTIFIER, "factory name expected");
        parser.skip(SPACE);
        parser.parse(LEFT_PARENTHESIS, "'(' expected");
        factoryParameters = parser.parse(FACTORY_PARAMETERS, "parameters expected");
        parser.parse(RIGHT_PARENTHESIS, "')' expected");

        result.addLibrary(identifier, constructLibrary(factory, factoryParameters), first);
        first=false;
        parser.skip(SPACE);

        if (!parser.isAtEnd()) {
          parser.parse(SEMICOLON, "; expected");
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
      throw new GeneratorFailure("Failed to construct generator library: " + e.getMessage(), e);
    }

    return result;
  }
}