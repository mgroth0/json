module matt.json {
    requires kotlin.stdlib.jdk8;
    requires kotlin.stdlib.jdk7;
    requires kotlin.reflect;

    requires com.google.gson;

    requires matt.kjlib;
    exports matt.json;
}