package git.service;

public interface GitService {

    void init(GitSessionWrapper session);
    void addRemote(GitSessionWrapper session);
    void pull(GitSessionWrapper session);
    void push(GitSessionWrapper session);
    void clone(GitSessionWrapper session);
    void fetchBranch(GitSessionWrapper session, String branch);
    void mergeBranch(GitSessionWrapper session, String fromBranch, String intoBranch);
    void revertToCommit(GitSessionWrapper session, String commit);
}
