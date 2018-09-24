package git.service;

class GitServiceException extends RuntimeException {
    public GitServiceException(String message, Throwable e){
        super(message,e);
    }

    public GitServiceException(String message){
        super(message);
    }
}
