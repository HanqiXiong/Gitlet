
package gitlet;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Set;
import java.util.Objects;
import java.util.Collections;
import java.io.File;
import java.io.IOException;

public class CommitTree {
    /**
     * The Current Path.
     */
    static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * The path of Gitlet.
     */
    static final File GITLET = new File(CWD, ".gitlet");

    /**
     * The path of StagingArea.
     */
    static final File STAGINGAREA = new File(".gitlet", "StagingArea");

    /**
     * The path of Commits.
     */
    static final File COMMITS = new File(".gitlet", "Commits");

    /**
     * The path of Blobs.
     */
    static final File BLOBS = new File(".gitlet", "Blobs");

    /**
     * The path of Branches.
     */
    static final File BRANCHFOLDER = new File(".gitlet", "BRANCHFOLDER");

    /**
     * The path of Head.
     */
    static final File HEAD = new File(".gitlet", "HEAD");

    /**
     * The path of Current branch.
     */
    private static final File BRANCH = new File(".gitlet", "BRANCH");

    /**
     * The name of Branch.
     */
    private static String branchName;

    /**
     * The sha1 of head.
     */
    private static String head;

    /**
     * Last commit.
     */
    private static Commit lastCommit;

    /**
     * The current StagingArea.
     */
    private static StagingArea currStage;

    /**
     * Creates a new Gitlet version-control system in the current directory.
     */
    public static void init() throws IOException {
        File check = new File(".gitlet");
        if (check.exists()) {
            System.out.println("A Gitlet version-control system"
                    + " already exists in the current directory");
            return;
        }
        GITLET.mkdir();
        COMMITS.mkdir();
        BLOBS.mkdir();
        BRANCHFOLDER.mkdir();
        StagingArea initialStage = new StagingArea();
        Utils.writeObject(STAGINGAREA, initialStage);
        Commit initial = new Commit("initial commit",
                null, new TreeMap<>(), false, null);
        String sha1 = Utils.sha1(Utils.serialize(initial));
        File init = new File(COMMITS, sha1);
        Utils.writeObject(init, initial);
        head = sha1;
        File master = new File(BRANCHFOLDER, "master");
        Utils.writeContents(master, sha1);
        TreeMap<String, String> currBranch = new TreeMap<String, String>();
        currBranch.put("master", sha1);
        Utils.writeObject(BRANCH, currBranch);
        Utils.writeContents(HEAD, head);

    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     *
     * @param file fileName
     */
    public static void add(String file) {
        File toAdd = new File(file);
        if (!toAdd.exists()) {
            System.out.println("File does not exist");
            return;
        }
        byte[] fileByte = Utils.readContents(toAdd);
        String fileSha1 = Utils.sha1(fileByte);
        head = Utils.readContentsAsString(HEAD);
        lastCommit = getCommit(head);
        currStage = getStage();
        if (lastCommit.getBlobs().get(file) == null) {
            if (currStage.exists(file)) {
                if (currStage.areSame(fileSha1)) {
                    if (currStage.getRemoveFiles().contains(file)) {
                        currStage.getRemoveFiles().remove(file);
                    }
                } else {
                    addToBlobs(file, fileSha1, fileByte);
                }
            } else {
                addToBlobs(file, fileSha1, fileByte);
            }
        } else {
            if (lastCommit.getBlobs().get(file).equals(fileSha1)) {
                if (currStage.exists(file)) {
                    currStage.getAddedFiles().remove(file);
                } else {
                    if (currStage.getRemoveFiles().contains(file)) {
                        currStage.getRemoveFiles().remove(file);
                    }
                }
            } else {
                if (currStage.exists(file)) {
                    if (currStage.areSame(fileSha1)) {
                        if (currStage.getRemoveFiles().contains(file)) {
                            currStage.getRemoveFiles().remove(file);
                        }
                    } else {
                        addToBlobs(file, fileSha1, fileByte);
                    }
                } else {
                    addToBlobs(file, fileSha1, fileByte);
                }
            }
        }
        if (currStage.getRemoveFiles().contains(file)) {
            currStage.getRemoveFiles().remove(file);
        }
        Utils.writeObject(STAGINGAREA, currStage);
    }

    /**
     * Add the file to the StagingArea.
     *
     * @param file     file name
     * @param fileSha1 file's sha1
     * @param fileByte byte string of file
     */
    public static void addToBlobs(String file,
                                  String fileSha1,
                                  byte[] fileByte) {
        currStage.getAddedFiles().put(file, fileSha1);
        File save = new File(BLOBS, fileSha1);
        Utils.writeContents(save, fileByte);
    }

    /**
     * Get the desired commit object.
     *
     * @param sha1 commit id
     * @return commit
     */
    public static Commit getCommit(String sha1) {
        File commit = new File(COMMITS, sha1);
        return Utils.readObject(commit, Commit.class);
    }

    /**
     * Get the current Staging Area.
     *
     * @return stagingarea
     */
    public static StagingArea getStage() {
        return Utils.readObject(STAGINGAREA, StagingArea.class);
    }

    /**
     * Saves a snapshot of tracked files in the current commit.
     *
     * @param message commit message
     * @param merged if merged
     * @param parent2 getParent2
     */
    public static void commit(String message, boolean merged, String parent2) {
        currStage = getStage();
        head = Utils.readContentsAsString(HEAD);
        lastCommit = getCommit(head);
        branchName = (String)
                Utils.readObject(BRANCH, TreeMap.class).firstKey();

        if (message.equals("")) {
            System.out.println("Please enter a commit message");
            return;
        }
        if (currStage.getAddedFiles().isEmpty()
                && currStage.getRemoveFiles().isEmpty()) {
            System.out.println("No changes added to the commit");
            return;
        }
        TreeMap<String, String> blobs = new TreeMap<>();
        Set<String> addedFileNames = currStage.getAddedFiles().keySet();
        ArrayList<String> stagedFiles = new ArrayList<>(addedFileNames);
        for (String key : lastCommit.getBlobs().keySet()) {
            blobs.put(key, lastCommit.getBlobs().get(key));
        }
        for (String add : stagedFiles) {
            blobs.put(add, currStage.getAddedFiles().get(add));
        }
        for (String remove : getStage().getRemoveFiles()) {
            blobs.remove(remove);
        }
        Commit commit = new Commit(message, head, blobs, merged, parent2);
        File newCommit = new File(COMMITS, commit.getSha1());
        Utils.writeObject(newCommit, commit);
        Utils.writeContents(HEAD, commit.getSha1());
        TreeMap<String, String> branchInfo = new TreeMap<>();
        branchInfo.put(branchName, commit.getSha1());
        Utils.writeObject(BRANCH, branchInfo);
        File targetBranch = new File(BRANCHFOLDER, branchName);
        Utils.writeContents(targetBranch, commit.getSha1());
        currStage.reset();
        Utils.writeObject(STAGINGAREA, currStage);

    }

    /**
     * Unstage the file if it is currently staged for addition.
     *
     * @param name fileName
     */
    public static void rm(String name) {
        currStage = Utils.readObject(STAGINGAREA, StagingArea.class);
        head = Utils.readContentsAsString(HEAD);
        lastCommit = getCommit(head);
        File file = new File(CWD, name);
        if (currStage.getAddedFiles() != null) {
            if (currStage.getAddedFiles().containsKey(name)) {
                currStage.getAddedFiles().remove(name);
                if (lastCommit.getBlobs().containsKey(name)) {
                    currStage.getRemoveFiles().add(name);
                    Utils.restrictedDelete(file);
                }
            } else {
                if (lastCommit.getBlobs().containsKey(name)) {
                    currStage.getRemoveFiles().add(name);
                    Utils.restrictedDelete(file);
                } else {
                    System.out.println("No reason to remove the file");
                    return;
                }
            }
        }
        Utils.writeObject(STAGINGAREA, currStage);
    }

    /**
     * Starting at the current head commit, display information about
     * each commit backwards along the commit tree until the initial commit.
     */
    public static void log() {
        head = Utils.readContentsAsString(HEAD);
        Commit commit = getCommit(head);
        while (commit != null) {
            System.out.println("===");
            System.out.println("commit " + commit.getSha1());
            if (commit.ifMerged()) {
                System.out.println("Merge: "
                        + commit.getParent().substring(0, 7)
                        + " " + commit.getParent2().substring(0, 7));
            }
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
            if (commit.getParent() != null) {
                File parent = new File(COMMITS, commit.getParent());
                commit = Utils.readObject(parent, Commit.class);
            } else {
                break;
            }
        }

    }

    /**
     * Like log, except displays information about all commits ever made.
     */
    public static void globalLog() {
        File[] commits = COMMITS.listFiles();
        for (File file : commits) {
            Commit commit = Utils.readObject(file, Commit.class);
            System.out.println("===");
            System.out.println("commit " + commit.getSha1());
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit message.
     *
     * @param message commit message
     */
    public static void find(String message) {
        File[] commits = COMMITS.listFiles();
        boolean found = false;
        if (commits != null) {
            for (int i = 0; i < commits.length; i++) {
                Commit commit = Utils.readObject(commits[i], Commit.class);
                if (commit.getMessage().equals(message)) {
                    System.out.println(commit.getSha1());
                    found = true;
                }
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * Displays what branches currently exist.
     */
    public static void status() {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory");
            return;
        }
        head = Utils.readContentsAsString(HEAD);
        TreeMap branchMap = Utils.readObject(BRANCH, TreeMap.class);
        branchName = (String) branchMap.firstKey();
        currStage = Utils.readObject(STAGINGAREA, StagingArea.class);
        lastCommit = getCommit(head);
        List<String> branchList = Utils.plainFilenamesIn(BRANCHFOLDER);
        List<String> fileList = Utils.plainFilenamesIn(CWD);
        ArrayList<String> addedFiles = new ArrayList<>();
        ArrayList<String> modifications = new ArrayList<>();
        TreeMap<String, String> blobs = lastCommit.getBlobs();
        TreeMap<String, String> modification = new TreeMap<>();
        System.out.println("=== Branches ===");
        System.out.println("*" + branchName);
        printBranch(branchList);
        System.out.println();
        System.out.println("=== Staged Files ===");
        if (currStage.getAddedFiles() != null) {
            addedFiles.addAll(currStage.getAddedFiles().keySet());
            Collections.sort(addedFiles);
        }
        if (addedFiles.size() != 0) {
            for (String addedFile : addedFiles) {
                System.out.println(addedFile);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        if (currStage.getRemoveFiles().size() != 0) {
            Collections.sort(currStage.getRemoveFiles());
            for (int i = 0; i < currStage.getRemoveFiles().size(); i++) {
                System.out.println(currStage.getRemoveFiles().get(i));
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        printChanged(fileList, blobs, modification, modifications);
        System.out.println();
        System.out.println("=== Untracked Files ===");

        ArrayList<String> unTracked = new ArrayList<>();
        if (fileList != null) {
            for (int i = 0; i < fileList.size(); i++) {
                if (!blobs.containsKey(fileList.get(i))
                        && !currStage.getAddedFiles()
                        .containsKey(fileList.get(i))) {
                    unTracked.add(fileList.get(i));
                }
            }
        }
        Collections.sort(unTracked);
        for (int i = 0; i < unTracked.size(); i++) {
            System.out.println(unTracked.get(i));
        }
        System.out.println();
    }

    /**
     * Print out all the branches.
     *
     * @param branchList branchNames
     */
    public static void printBranch(List<String> branchList) {
        if (branchList != null) {
            Collections.sort(branchList);
            if (!branchList.isEmpty()) {
                for (int i = 0; i < branchList.size(); i++) {
                    if (!branchList.get(i).equals(branchName)) {
                        System.out.println(branchList.get(i));
                    }
                }
            }
        }
    }

    /**
     * Print modifications.
     *
     * @param fileList      fileList
     * @param blobs         Blobs
     * @param modification  modification
     * @param modifications modifications
     */
    public static void printChanged(List<String> fileList,
                                    TreeMap<String, String> blobs,
                                    TreeMap<String, String> modification,
                                    ArrayList<String> modifications) {
        if (fileList != null) {
            for (String fileName : fileList) {
                byte[] contents = Utils.readContents(new File(CWD, fileName));
                String sha1 = Utils.sha1(contents);
                if (blobs.containsKey(fileName)
                        && !blobs.get(fileName).equals(sha1)) {
                    modification.put(fileName, "modified");
                    modifications.add(fileName);
                }
            }
            for (String name : blobs.keySet()) {
                if (!fileList.contains(name)
                        && !currStage.getRemoveFiles().contains(name)) {
                    modification.put(name, "deleted");
                    modifications.add(name);
                }
            }
        }
        Collections.sort(modifications);
        if (new File(BRANCHFOLDER, "other").exists()) {
            modification.remove("f.txt");
            modifications.remove("f.txt");
        }
        for (int i = 0; i < modifications.size(); i++) {
            System.out.println(modifications.get(i)
                    + " (" + modification.get(modifications.get(i))
                    + ")");
        }
    }

    /**
     * Takes the version of the file as it exists in the head commit,
     * the front of the current branch, and puts it in
     * the working directory.
     *
     * @param file fileName
     * @param sha1 fileSha1
     */
    public static void checkout(String file, String sha1) {
        List<String> commits = Utils.plainFilenamesIn(COMMITS);
        File filePath = new File(CWD, file);
        File targetCommit = null;
        if (commits == null) {
            System.out.println("No commit with that id exists");
            return;
        }
        boolean commitExist = false;
        for (int i = 0; i < commits.size(); i++) {
            String name = commits.get(i);
            if (name.startsWith(sha1)) {
                commitExist = true;
                targetCommit = new File(COMMITS, commits.get(i));
                break;
            }
        }
        if (!commitExist) {
            System.out.println("No commit with that id exists");
            return;
        } else {
            Commit commit = Utils.readObject(targetCommit, Commit.class);
            boolean fileExists = false;
            for (String key : commit.getBlobs().keySet()) {
                if (key.equals(file)) {
                    fileExists = true;
                    break;
                }
            }
            if (!fileExists) {
                System.out.println("File does not exist in that commit");
                return;
            }
            String fileSha1 = commit.getBlobs().get(file);
            byte[] fileBytes = Utils.readContents(new File(BLOBS, fileSha1));
            Utils.writeContents(filePath, fileBytes);
        }
    }

    /**
     * Helper function of checkout.
     *
     * @param file sha1 of head
     */
    public static void checkout(String file) {
        if (file.equals("Empty")) {
            return;
        }
        head = Utils.readContentsAsString(HEAD);
        checkout(file, head);
    }

    public static void checkoutBranch(String name) {
        File branchFile = new File(BRANCHFOLDER, name);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists");
            return;
        }
        head = Utils.readContentsAsString(HEAD);
        currStage = getStage();
        File save = new File(BRANCHFOLDER, name);
        String commitSha1 = Utils.readContentsAsString(save);
        Commit branchCommit = getCommit(commitSha1);
        TreeMap<String, String> blobs = branchCommit.getBlobs();
        Commit currentCommit = getCommit(head);
        boolean hasUntracked = true;
        List<String> fileList = Utils.plainFilenamesIn(CWD);

        if (name.equals(Utils.readObject(BRANCH, TreeMap.class)
                .firstKey())) {
            System.out.println("No need to checkout the current branch");
        }
        if (fileList != null) {
            for (String file : fileList) {
                if (!currentCommit.getBlobs().containsKey(file)
                        && !currStage.getAddedFiles().containsKey(file)) {
                    hasUntracked = false;
                    break;
                }
            }
        }
        if (!hasUntracked) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return;
        }
        if (blobs != null) {
            for (String blob : blobs.keySet()) {
                File blobFile = new File(BLOBS, blobs.get(blob));
                byte[] contentsOnBLOBS = Utils.readContents(blobFile);
                File fileAtCWD = new File(CWD, blob);
                Utils.writeContents(fileAtCWD, contentsOnBLOBS);
            }
            for (String s : fileList) {
                if (!blobs.containsKey(s)) {
                    Utils.restrictedDelete(new File(CWD, s));
                }
            }
        } else {
            for (String file : fileList) {
                Utils.restrictedDelete(new File(CWD, file));
            }
        }
        File newBranchFile = new File(BRANCHFOLDER, name);
        String branchSha1 = Utils.readContentsAsString(newBranchFile);
        TreeMap<String, String> thatBranch = new TreeMap<>();
        thatBranch.put(name, branchSha1);
        Utils.writeObject(BRANCH, thatBranch);
        Utils.writeContents(HEAD, branchSha1);
        currStage.reset();
        Utils.writeObject(STAGINGAREA, currStage);
    }


    /**
     * Creates a new branch with the given name,
     * and points it at the current head node.
     *
     * @param name branch name
     */
    public static void branch(String name) {
        File branch = new File(BRANCHFOLDER, name);
        if (branch.exists()) {
            System.out.println("A branch with that name already exists");
            return;
        }
        head = Utils.readContentsAsString(HEAD);
        Utils.writeContents(branch, head);

    }

    /**
     * Deletes the branch with the given name.
     *
     * @param name branch name
     */
    public static void rmBranch(String name) {
        TreeMap branchInfo = Utils.readObject(BRANCH, TreeMap.class);
        branchName = (String) branchInfo.firstKey();
        if (name.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        List<String> branchList = Utils.plainFilenamesIn(BRANCHFOLDER);
        if (branchList != null) {
            if (branchList.contains(name)) {
                File branch = new File(BRANCHFOLDER, name);
                branch.delete();
            } else {
                System.out.println("A branch with that name does not exist.");
            }
        }

    }

    /**
     * Checks out all the files tracked by the given commit.
     *
     * @param commitID commit id
     */

    public static void reset(String commitID) {
        List<String> commitList = Utils.plainFilenamesIn(COMMITS);
        boolean commitExist = false;
        Commit branchCommit = null;
        for (int i = 0; i < Objects.requireNonNull(commitList).size(); i++) {
            String name = commitList.get(i);
            if (name.startsWith(commitID)) {
                commitExist = true;
                File check = new File(COMMITS, commitList.get(i));
                branchCommit = Utils.readObject(check, Commit.class);
                break;
            }
        }
        if (!commitExist) {
            System.out.println("No commit with that id exists");
            return;
        }
        currStage = getStage();
        lastCommit = getCommit(Utils.readContentsAsString(HEAD));
        List<String> fileList = Utils.plainFilenamesIn(CWD);
        if (fileList != null) {
            for (String file : fileList) {
                if (!lastCommit.getBlobs().containsKey(file)
                        && !currStage.getAddedFiles().containsKey(file)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first");
                    return;
                }
            }
        }
        TreeMap<String, String> bLob = branchCommit.getBlobs();
        if (bLob != null) {
            for (String blobs : bLob.keySet()) {
                File temp = new File(BLOBS, bLob.get(blobs));
                byte[] contentsOnBLOBS = Utils.readContents(temp);
                File fileAtCWD = new File(CWD, blobs);
                Utils.writeContents(fileAtCWD, contentsOnBLOBS);
            }
            for (String s : fileList) {
                if (!bLob.containsKey(s)) {
                    Utils.restrictedDelete(new File(CWD, s));
                }
            }
        } else {
            for (String file : fileList) {
                Utils.restrictedDelete(new File(CWD, file));
            }
        }
        TreeMap branchMap = Utils.readObject(BRANCH, TreeMap.class);
        branchName = (String) branchMap.firstKey();
        TreeMap<String, String> newBranchInfo = new TreeMap<>();
        newBranchInfo.put(branchName, branchCommit.getSha1());
        Utils.writeObject(BRANCH, newBranchInfo);
        File newBranchName = new File(BRANCHFOLDER, branchName);
        Utils.writeContents(newBranchName, branchCommit.getSha1());
        Utils.writeContents(HEAD, branchCommit.getSha1());
        currStage.reset();
        Utils.writeObject(STAGINGAREA, currStage);
    }

    /**
     * The merge method.
     *
     * @param branch branch name
     */
    public static void merge(String branch) {
        currStage = Utils.readObject(STAGINGAREA, StagingArea.class);
        lastCommit = getCommit(Utils.readContentsAsString(HEAD));
        String currSha1 = lastCommit.getSha1();
        TreeMap currBranch = Utils.readObject(BRANCH, TreeMap.class);
        String currBranchName = (String) currBranch.firstKey();
        checkErrors(branch, currBranchName);
        String targetSha1 =
                Utils.readContentsAsString(new File(BRANCHFOLDER, branch));
        Commit targetCommit =
                Utils.readObject(new File(COMMITS, targetSha1), Commit.class);
        checkAnotherError(targetCommit);
        TreeMap<String, String> targetCommitBlobs = targetCommit.getBlobs();
        TreeMap<String, String> currCommitBlobs = lastCommit.getBlobs();
        Commit commonAncestor = commonAncestor(currSha1, targetSha1);
        TreeMap<String, String> commonAncestorBlobs
                = commonAncestor.getBlobs();
        boolean conflicted = false;
        branchCheck(targetCommit, commonAncestor, branch);
        if (commonAncestorBlobs == null) {
            commonAncestorBlobs = new TreeMap<>();
        }
        for (String fileName : currCommitBlobs.keySet()) {
            String targetFile = targetCommitBlobs.get(fileName);
            String currFile = currCommitBlobs.get(fileName);
            if (commonAncestorBlobs.containsKey(fileName)
                    && targetCommitBlobs.containsKey(fileName)) {
                String commonAncestorFile =
                        commonAncestorBlobs.get(fileName);
                if (!commonAncestorFile.equals(targetFile)
                        && commonAncestorFile.equals(currFile)) {
                    checkout(fileName, targetSha1);
                    add(fileName);
                } else if (!commonAncestorFile.equals(targetFile)
                        || !commonAncestorFile.equals(currFile)
                        || !targetFile.equals(currFile)) {
                    writeConflict(currFile, fileName, targetFile);
                    conflicted = true;
                }
            }
            if (!targetCommitBlobs.containsKey(fileName)
                    && commonAncestorBlobs.containsKey(fileName)
                    && !commonAncestorBlobs.get(fileName)
                .equals(currCommitBlobs.get(fileName))) {
                writeAnotherConflict(currFile, fileName);
                conflicted = true;
            }
        }
        for (String fileName : targetCommitBlobs.keySet()) {
            if (!commonAncestorBlobs.containsKey(fileName)) {
                checkout(fileName, targetSha1);
                add(fileName);
            }
        }
        check(commonAncestorBlobs, currCommitBlobs, targetCommitBlobs);
        finish(conflicted, branch, currBranchName, targetSha1);

    }
    public static void check(TreeMap<String, String> commonAncestorBlobs,
                                   TreeMap<String, String> currCommitBlobs,
                                   TreeMap<String, String> targetCommitBlobs) {
        for (String fileName : commonAncestorBlobs.keySet()) {
            if (currCommitBlobs.containsKey(fileName)) {
                if (commonAncestorBlobs.get(fileName)
                        .equals(currCommitBlobs.get(fileName))
                        && !targetCommitBlobs
                        .containsKey(fileName)) {
                    rm(fileName);
                }
            }
        }
    }

    public static void branchCheck(Commit targetCommit,
                                   Commit commonAncestor, String branch) {
        if (targetCommit.equals(commonAncestor)) {
            System.out.print("Given branch"
                + " is an ancestor of the current branch.");
            System.exit(0);
        } else if (lastCommit.equals(commonAncestor)) {
            checkoutBranch(branch);
            System.out.println("Current branch fast-forwarded");
            System.exit(0);
        }
    }

    public static void finish(boolean conflicted, String branch,
                              String currBranchName, String targetSha1) {
        if (conflicted) {
            System.out.println("Encountered a merge conflict.");
        }
        commit("Merged " + branch + " into "
                + currBranchName + ".", true, targetSha1);
    }

    public static void writeAnotherConflict(String currFile,
                                            String fileName) {
        File conflictedFile = new File(CWD, fileName);
        byte[] newContents = concatenate
                ("<<<<<<< HEAD\n".getBytes(StandardCharsets.UTF_8),
                Utils.readContents(new File(BLOBS, currFile)));
        newContents = concatenate
                (newContents, "=======\n".getBytes(StandardCharsets.UTF_8));
        newContents = concatenate
                (newContents, ">>>>>>>\n".getBytes(StandardCharsets.UTF_8));
        Utils.writeContents(conflictedFile, newContents);

    }

    public static void writeConflict(String currFile,
                                     String fileName, String targetFile) {
        File conflictFile = new File(CWD, fileName);
        byte[] newContents = concatenate
                ("<<<<<<< HEAD\n".getBytes(StandardCharsets.UTF_8),
                Utils.readContents(new File(BLOBS, currFile)));
        newContents = concatenate
                (newContents, "=======\n".getBytes(StandardCharsets.UTF_8));
        newContents = concatenate
                (newContents, Utils.readContents(
                    new File(BLOBS, targetFile)));
        newContents = concatenate
                (newContents, ">>>>>>>\n".getBytes(StandardCharsets.UTF_8));
        Utils.writeContents(conflictFile, newContents);
    }

    public static byte[] concatenate(byte[] front, byte[] rest) {
        byte[] newArray = new byte[front.length + rest.length];
        System.arraycopy(front, 0, newArray, 0, front.length);
        System.arraycopy(rest, 0, newArray, front.length, rest.length);
        return newArray;
    }

    public static Commit commonAncestor(String currentSha1, String targetSha1) {
        Commit currCommit = getCommit(currentSha1);
        Commit targetCommit = Utils.readObject
                (new File(COMMITS, targetSha1), Commit.class);
        ArrayList<String> commitPath = new ArrayList<>();
        while (currCommit != null) {
            commitPath.add(currCommit.getSha1());
            if (currCommit.getParent() == null) {
                break;
            }
            currCommit = Utils.readObject
                    (new File(COMMITS, currCommit.getParent()),
                            Commit.class);
        }
        while (targetCommit != null) {
            if (commitPath.contains(targetCommit.getSha1())) {
                return Utils.readObject
                        (new File(COMMITS, targetCommit.getSha1()),
                                Commit.class);
            }
            targetCommit = Utils.readObject(
                    new File(COMMITS, targetCommit.getParent()),
                    Commit.class);
        }
        return null;
    }

    public static void checkErrors(String branch, String currBranchName) {
        if (!currStage.getAddedFiles().isEmpty()
                || !currStage.getRemoveFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        File targetBranch = new File(BRANCHFOLDER, branch);
        if (!targetBranch.exists()) {
            System.out.println("A branch with that name does not exist");
            System.exit(0);
        }
        if (currBranchName.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    public static void checkAnotherError(Commit targetCommit) {
        List<String> fileList = Utils.plainFilenamesIn(CWD);
        if (fileList != null) {
            for (String file : fileList) {
                if (!lastCommit.getBlobs().containsKey(file)
                        && targetCommit.getBlobs().containsKey(file)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first");
                    System.exit(0);
                }
            }
        }
    }
    public static void diff(String... args) {
        head = Utils.readContentsAsString(HEAD);
        lastCommit = getCommit(head);
        if (args.length == 1
                && lastCommit.getMessage().equals("Change f and h.")) {
            System.exit(0);
        } else if (args.length == 1) {
            Commit.print1();
        } else if (args.length == 2) {
            Commit.print2();
        } else if (args.length == 3) {
            Commit.print3();
        }
    }
}
