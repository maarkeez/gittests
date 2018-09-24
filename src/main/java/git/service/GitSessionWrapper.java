package git.service;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class GitSessionWrapper {

    private Path repositoryPath;
    private String username;
    private String password;
    private String url;
}
