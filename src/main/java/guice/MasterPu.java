package guice;

import static java.lang.annotation.ElementType.*;

@javax.inject.Qualifier
@java.lang.annotation.Target({FIELD, PARAMETER, METHOD})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface MasterPu {
}
