package guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.persist.PersistFilter;

public class MyModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new MasterPuModule());
        install(new SlavePuModule());
    }

    private static class MasterPuModule extends JpaPersistPrivateModule {
        public MasterPuModule() {
            super("masterPU", MasterPu.class);
        }

        @Override
        protected void doConfigure() {
            bindConcreteClassWithQualifier(MyTableService.class);

            Key<PersistFilter> key = Key.get(PersistFilter.class, qualifier);
            bind(key).to(PersistFilter.class);
            expose(key);
        }
    }

    private static class SlavePuModule extends JpaPersistPrivateModule {
        public SlavePuModule() {
            super("slavePU", SlavePu.class);
        }

        @Override
        protected void doConfigure() {
            bindConcreteClassWithQualifier(MyTableService.class);
        }
    }
}
