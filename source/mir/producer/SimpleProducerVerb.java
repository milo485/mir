package mir.producer;

public class SimpleProducerVerb implements ProducerFactory.ProducerVerb {
  private String name;
  private String description;

  public SimpleProducerVerb(String aName, String aDescription) {
    super();

    name = aName;
    description = aDescription;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}