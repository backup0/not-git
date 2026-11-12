package pk2;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.nio.file.*;

public class Main extends Application {

    /*
    You can't just wipe all files/folders inside the user's project folder -- that's crazy you know, -- why?
    what if the user has put some files in there? -- you can't just wipe the whole thing, -- that's madness. so, --
    i mean when checking out, you can't just delete everything; what if the user has added, so need to run status check
    first - need to scan first. So - so you should only delete ''; what should I delete? I'm -- Ok, say I put some files
    in teh project folder -- yeah, now i want to check out; I could loose those files if they're not; so -- so we need
    to scan first, fuck man this thing is way more complicated than I think - yes, it is isn't it, it's not so simple.
    Maybe, maybe that's why people are not making, I do -- fuIck that shit, imma do it, and imma gonna succeed.
    Ok, say I don't want to track this file -- you don't track the content; you don't care if it's changed or not.

    I was thinking about this, now I can't remember shit. -- Ok, say you add some new files, and now you want to check out.
    when you check out, the stuff in your project folder will be the same as the stuff inside the repo. Yes, -- what will
    happen then to new files you create? Ok, the easiest option is to just wipe the entire project folder, then pull files
    from the db, b00m, poofs; all good, - but what if you have edited files etc. maybe that's why git asks you to commit first
    lorz; linus thought of that? -- I thought of that on my own,
     */

    /*
     final String qDaily = "create table daily(id int primary key, deleted boolean default false, entry blob, indexed boolean default false, updated int);";
    final String qLoose = "create table loose(id int generated always as identity primary key, name varchar not null, entry blob, odr int, indexed boolean default false, created integer, updated integer, deleted boolean default false, editable boolean default true);";
    final String qSettings = "create table settings(name varchar primary key, varchar val);";
     */

    @Override
    public void start(Stage primaryStage)
    {
        //VBox root = new VBox();
        //primaryStage.setResizable(false);
        //Screen.getPrimary().
        try
        {
            //Global.thisBranchDB = new Hush(Paths.get("D:\\Disposable\\not_git"), "ng_sub_2");
            //Global.extBranchDB = new Hush(Paths.get("D:\\Disposable\\not_git"), "ng_sub");
            //Global.thisBranchDB = new Hush(Paths.get("D:\\Disposable\\not_git_2"), "");
            Global.imgs = new Images();

            //w(Global.getChecksum(Paths.get("D:\\Disposable\\Opera Snapshot_2025-06-19_200512_www.ef.com.png")));
            /*
            Path p1 = Paths.get("D:\\Disposable\\diff_t\\this is text.txt");
            Calendar d = Calendar.getInstance();
            d.set(2024, 2, 17);
            Files.setLastModifiedTime(p1, FileTime.fromMillis(d.getTimeInMillis()));
            */
        }
        catch (Exception e) {e.printStackTrace();}

        primaryStage.setTitle("Hello World");


        try
        {
            Application.setUserAgentStylesheet(getClass().getResource("/rsc/cupertino-light.css").toExternalForm());
        }
        catch (Exception ignore) {}

        //primaryStage.setScene(new Scene(new FCGui(), 500, 500));
        //final Scene scene = new Scene(new CPS(), 500, 500);
        final Scene scene = new Scene(new SuperMainPage(), 500, 500);
        //final Scene scene = new Scene(new DiffBox(), 500, 500);
        //final Scene scene = new Scene(new Diffy(), 850, 500);
        //final Scene scene = new Scene(new FirstPage(), 500, 500);
        //final Scene scene = new Scene(new TreeV(), 500, 500);
        //final Scene scene = new Scene(new TestDiff(), 500, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent windowEvent)
            {
                try
                {
                    // otherwise some windows won't close -- dehs!
                    //System.exit(0);
                }
                catch (Exception e) {};
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void w(Object... t) {
        StringBuilder sb = new StringBuilder();
        for (Object s : t) {
            sb.append(s);
        }
        System.out.println(sb.toString());
    }
}
