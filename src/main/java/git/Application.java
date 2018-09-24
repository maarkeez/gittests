package git;

import git.service.GitService;
import git.service.GitServiceJGit;
import git.service.GitSessionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class Application {

    public static void main(String[] args){
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory("gittests").toAbsolutePath();
            log.info("Temporal directory: {}",tmp);

            GitSessionWrapper session =  GitSessionWrapper.builder()
                    .password("a2419877121b68883c33161e05e04be0637b3e27")
                    .username("maarkeez")
                    .url("https://github.com/maarkeez/gittests.git")
                    .repositoryPath(tmp)
                    .build();

            GitService gitService = new GitServiceJGit();

            gitService.init(session);
            gitService.addRemote(session);
            gitService.fetchBranch(session,"master");
            gitService.pull(session);
            gitService.revertToCommit(session,"0e03f141784e3edfdd4111759aa99c34cb1d4642");
            gitService.push(session);

            // gitService.clone(session);
            gitService.fetchBranch(session,"branch1");
            gitService.mergeBranch(session,"branch1","master");
            gitService.push(session);


        }catch (Exception e){
            log.error("Main method error {}", e);
        }finally {
            log.info("Finished!");
            if(tmp!=null)tmp.toFile().delete();
        }
    }
}
