package mir.util;

import java.util.List;

import mir.generator.Generator;
import mir.generator.GeneratorExc;
import mir.generator.GeneratorFailure;

public class GeneratorIntegerFunctions {

  private GeneratorIntegerFunctions() {}

  public static class incrementFunction implements Generator.GeneratorFunction {
    public Object perform(List aParameters) throws GeneratorExc, GeneratorFailure {
      int incrementValue = 1;

      try {
        if (aParameters.size()>2 || aParameters.size()<1)
          throw new GeneratorExc("incrementFunction: 1 or 2 parameters expected: value [increment value]");

        if (aParameters.size()>1)
          incrementValue = StringRoutines.interpretAsInteger(aParameters.get(1));

        return new Integer(StringRoutines.interpretAsInteger(aParameters.get(0)) + incrementValue);
      }
      catch (GeneratorExc e) {
        throw e;
      }
      catch (Throwable t) {
        throw new GeneratorFailure("incrementFunction: " + t.getMessage(), t);
      }
    };
  }

  public static class isOddFunction implements Generator.GeneratorFunction {
    public Object perform(List aParameters) throws GeneratorExc, GeneratorFailure {
      try {
        if (aParameters.size()!=1)
          throw new GeneratorExc("isOddFunction: 1 parameters expected: value");

        return new Boolean((StringRoutines.interpretAsInteger(aParameters.get(0)) & 1) == 1);
      }
      catch (GeneratorExc e) {
        throw e;
      }
      catch (Throwable t) {
        throw new GeneratorFailure("isOddFunction: " + t.getMessage(), t);
      }
    };
  }
}