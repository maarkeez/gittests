package git.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.List;

@Slf4j
public class GitServiceJGit implements GitService {


    @Override
    public void init(GitSessionWrapper session) {

        try {
            // prepare a new folder
            File localPath = session.getRepositoryPath().toFile();
            localPath.delete();

            // create the directory
            Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
            repository.create();

        } catch (Exception e) {
            throw new GitServiceException("Could not init repository", e);
        }

    }

    @Override
    public void addRemote(GitSessionWrapper session) {
        try {
            Git git = Git.open(session.getRepositoryPath().toFile());
            git.remoteAdd().setUri(new URIish(session.getUrl())).setName("origin").call();
            git.fetch().call();

        } catch (Exception e) {
            throw new GitServiceException("Could not add remote repository", e);
        }
    }

    @Override
    public void pull(GitSessionWrapper session) {
        try {
            Git git = Git.open(session.getRepositoryPath().toFile());
            git.pull().setRemote("origin").call();

        } catch (Exception e) {
            throw new GitServiceException("Could not pull repository", e);
        }
    }

    @Override
    public void push(GitSessionWrapper session) {
        try {
            Git git = Git.open(session.getRepositoryPath().toFile());
            git.push().setRemote("origin")
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(session.getUsername(),session.getPassword()))
                    .call();

        } catch (Exception e) {
            throw new GitServiceException("Could not push branch to remote", e);
        }
    }

    @Override
    public void clone(GitSessionWrapper session) {
        try {

            FileUtils.deleteDirectory(session.getRepositoryPath().toFile());
            session.getRepositoryPath().toFile().mkdir();

            Git.cloneRepository()
                    .setDirectory(session.getRepositoryPath().toFile())
                    .setURI(session.getUrl())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(session.getUsername(), session.getPassword()))
                    .call();

        } catch (Exception e) {
            throw new GitServiceException("Could not clone repository", e);
        }
    }

    @Override
    public void fetchBranch(GitSessionWrapper session, String branch) {
        try {
            Git git = Git.open(session.getRepositoryPath().toFile());

            List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
            branches.stream().parallel()
                    .filter(b -> b.getName().endsWith(branch))
                    .map(b -> b.getName())
                    .findFirst()
                    .ifPresent(name -> {
                        try {
                            git.checkout().setCreateBranch(true).setName(branch).setStartPoint(name).call();
                            git.pull().call();
                        } catch (GitAPIException e) {
                            throw new GitServiceException("Could not fetch branch", e);
                        }
                    });

        } catch (Exception e) {
            throw new GitServiceException("Could not fetch branch", e);
        }
    }

    @Override
    public void mergeBranch(GitSessionWrapper session, String fromBranch, String intoBranch) {
        try {
            Git git = Git.open(session.getRepositoryPath().toFile());

            git.checkout().setName(intoBranch).setCreateBranch(false).call();
            ObjectId fromBranchRef = git.getRepository().resolve(fromBranch);
            MergeResult res = git.merge().include(fromBranchRef).setCommit(true).setMessage("Merge branch " + fromBranch + " into branch " + intoBranch).call();
            if (res.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)){
                throw new GitServiceException("Could not merge branch. Conflicting: " +res.getConflicts().toString());
            }

        } catch (Exception e) {
            throw new GitServiceException("Could not merge branch", e);
        }
    }

    @Override
    public void revertToCommit(GitSessionWrapper session, String commit) {
        try {

            Git git = Git.open(session.getRepositoryPath().toFile());
            ObjectId commitId = git.getRepository().resolve(commit);
            git.revert().include(commitId).call();

        } catch (Exception e) {
            throw new GitServiceException("Could not reset to commit", e);
        }
    }
}
