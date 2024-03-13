# JSON Logic Java Implementation

This project provides a Java implementation of JSON Logic, offering a powerful and flexible way to apply logic expressed in JSON format. Designed with simplicity and efficiency in mind, this single-class library requires Java 8 or higher and seamlessly integrates with any Java project.

## Features

- **Effortless Integration**: As a single-class solution, integrating it into your project is straightforward.
- **Java 8+ Compatibility**: Ensures compatibility with Java 8 and newer versions, leveraging modern Java features.
- **Robust JSON Logic Processing**: Capable of evaluating complex JSON Logic expressions, making it versatile for various applications.

## Getting Started

### Prerequisites

Before integrating this library into your project, ensure you have Java Development Kit (JDK) 8 or newer installed on your system.

### Installation

**Maven:**

To include this library in your Maven project, add the following dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>com.sciumo</groupId>
    <artifactId>jjsonlogic</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**

For Gradle projects, add this dependency to your `build.gradle` file:

```gradle
implementation 'com.sciumo:jjsonlogic:1.0.0'
```

### Basic Usage

To use `jjsonlogic` in your Java application, you'll need to instantiate the `JsonLogic` class and use its evaluation method. Here is a simple example:

```java
import com.sciumo.jjsonlogic.JsonLogic;

public class Example {
    public static void main(String[] args) {
        JsonLogic jsonLogic = new JsonLogic();
        String rule = "{\"==\": [1, 1]}";
        boolean result = jsonLogic.evaluate(rule);
        System.out.println("Evaluation Result: " + result); // Output: Evaluation Result: true
    }
}

```

## Contributing

Contributions to `jjsonlogic` are welcome! Whether it's reporting bugs, discussing improvements, or submitting pull requests, all contributions help improve this library.

## License

This project is licensed under the [Apache 2.0](LICENSE). Feel free to use it, contribute, or share it as you see fit.

## Contact

For any questions or suggestions regarding `jjsonlogic`, please feel free to contact us via [GitHub Issues](https://github.com/Sciumo/jjsonlogic/issues).

---

Remember to replace placeholders like `https://github.com/Sciumo/jjsonlogic/issues` with actual URLs relevant to your project. This `README.md` provides a basic overview and starting point for users interested in using or contributing to your JSON Logic implementation.