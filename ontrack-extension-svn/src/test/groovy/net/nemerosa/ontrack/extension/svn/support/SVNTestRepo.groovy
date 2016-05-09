package net.nemerosa.ontrack.extension.svn.support

import org.apache.commons.io.FileUtils
import org.junit.Assert

import static net.nemerosa.ontrack.common.Utils.run

class SVNTestRepo {

    private final String repoName
    private File repo
    private int port
    private long pid

    SVNTestRepo(String repoName) {
        this.repoName = repoName
    }

    static SVNTestRepo get(String repoName) {
        SVNTestRepo repo = new SVNTestRepo(repoName)
        repo.start()
        repo
    }

    private static int findFreePort() {
        ServerSocket socket = new ServerSocket(0)
        try {
            return socket.getLocalPort()
        } finally {
            socket.close()
        }
    }

    long start() {
        repo = new File("build/repo/$repoName").absoluteFile
        FileUtils.deleteQuietly(repo)
        repo.mkdirs()
        println "SVN Test repo at ${repo.absolutePath}"
        // Creates the repository
        run repo, "svnadmin", "create", repo.absolutePath
        // Configuration file
        FileUtils.writeByteArrayToFile(new File(repo, 'conf/authz'), SVNTestUtils.class.getResourceAsStream('/svn/conf/authz').bytes)
        FileUtils.writeByteArrayToFile(new File(repo, 'conf/passwd'), SVNTestUtils.class.getResourceAsStream('/svn/conf/passwd').bytes)
        FileUtils.writeByteArrayToFile(new File(repo, 'conf/svnserve.conf'), SVNTestUtils.class.getResourceAsStream('/svn/conf/svnserve.conf').bytes)
        // Random port
        port = findFreePort()
        // Starts serving the repository
        def pidFile = new File(repo, 'pid')
        run repo, "svnserve", "--daemon", "--listen-port", port as String, "--root", repo.absolutePath, "--pid-file", pidFile.absolutePath
        // Waits until the PID is created
        boolean pidExists = pidFile.exists()
        int tries = 0
        while (!pidExists && tries < 5) {
            println "Waiting for SVN repo at ${repo.absolutePath} to start..."
            sleep 1000
            pidExists = pidFile.exists()
            tries++
        }
        if (!pidExists) {
            Assert.fail "The SVN repo at ${repo.absolutePath} could not start in 5 seconds."
        } else {
            this.pid = pidFile.text as long
            println "SVN server started in $repo with pid=$pid on port $port"
            // OK
            pid
        }
    }

    String getUrl() {
        return "svn://localhost:${port}"
    }

    void stop() {
        if (pid != 0) {
            println "Stopping server with pid=$pid"
            run repo, "kill", "-KILL", "$pid"
        }
    }

    def mkdir(String path, String message) {
        run repo, 'svn', 'mkdir', '--message', message, '--parents', '--username', 'user', '--password', 'test', '--no-auth-cache', "${url}/${path}"
    }

    def file(String path, String content, String message) {
        File wc = File.createTempFile("wc-${repoName}", '.d')
        FileUtils.deleteQuietly(wc)
        wc.mkdirs()
        try {
            // Download
            run wc, 'svn', 'checkout', url, wc.absolutePath
            // Edition
            File file = new File(wc, path)
            file.parentFile.mkdirs()
            file.text = content
            // Addition and commit
            run wc, 'svn', 'add', '--force', '--parents', path
            run wc, 'svn', 'commit', '.', '--message', message, '--username', 'user', '--password', 'test', '--no-auth-cache'
        } finally {
            FileUtils.deleteQuietly(wc)
        }
    }

    /**
     * Merges {@code from} into {@code to} using the {@code wd} working directory.
     */
    def merge(File wd, String from, String to, String message) {
        FileUtils.cleanDirectory wd
        // Checks the from out
        run wd, 'svn', 'checkout', "${url}/${to}", wd.absolutePath
        // Merge the to
        run wd, 'svn', 'merge', '--accept', 'postpone', "${url}/${from}", wd.absolutePath
        // Commit
        run wd, 'svn', 'commit', '--message', message, wd.absolutePath, '--username', 'user', '--password', 'test', '--no-auth-cache'
    }

    /**
     * Remote copy of {@code from} into {@code into} using the {@code message} message.
     */
    def copy(String from, String into, String message) {
        run repo, 'svn', 'copy', '--parents', "${url}/${from}", "${url}/${into}", '--message', message, '--username', 'user', '--password', 'test', '--no-auth-cache'
    }
}
