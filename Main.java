package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Hanqi Xiong
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            CommitTree.init();
            break;
        case "add":
            CommitTree.add(args[1]);
            break;
        case "commit":
            CommitTree.commit(args[1], false, null);
            break;
        case "rm":
            CommitTree.rm(args[1]);
            break;
        case "log":
            CommitTree.log();
            break;
        case "global-log":
            CommitTree.globalLog();
            break;
        case "find":
            CommitTree.find(args[1]);
            break;
        case "status":
            CommitTree.status();
            break;
        case "checkout":
            checkoutHelper(args);
        case "branch":
            CommitTree.branch(args[1]);
            break;
        case "rm-branch":
            CommitTree.rmBranch(args[1]);
            break;
        case "reset":
            CommitTree.reset(args[1]);
            break;
        case "merge":
            CommitTree.merge(args[1]);
            break;
        case "diff":
            CommitTree.diff(args);
            break;
        default:
            System.out.println("No command with that name exists");
        }
    }

    public static void checkoutHelper(String... args) {
        if (args.length - 1 == 1) {
            CommitTree.checkoutBranch(args[1]);
            System.exit(0);
        } else if (args.length - 1 == 2) {
            CommitTree.checkout(args[2]);
            System.exit(0);
        } else if (args.length - 1 == 3) {
            if (args[2].equals("--")) {
                CommitTree.checkout(args[3], args[1]);
                System.exit(0);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        }
    }
}
