package xyz.lychee.lagfixer;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class LagFixerLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver mavenResolver = new MavenLibraryResolver();

        mavenResolver.addRepository(
                new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build()
        );

        mavenResolver.addRepository(
                new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build()
        );

        mavenResolver.addDependency(
                new Dependency(new DefaultArtifact("org.apache.commons:commons-lang3:3.20.0"), null)
        );

        mavenResolver.addDependency(
                new Dependency(new DefaultArtifact("commons-io:commons-io:2.22.0"), null)
        );

        mavenResolver.addDependency(
                new Dependency(new DefaultArtifact("com.github.oshi:oshi-core:7.3.1"), null)
        );

        classpathBuilder.addLibrary(mavenResolver);
    }
}