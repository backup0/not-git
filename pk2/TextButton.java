package pk2;


import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class TextButton extends Label
{
    public TextButton(String s)
    {
        super(s);
        setTextFill(Color.DARKORANGE);

        setOnMousePressed(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent mouseEvent)
            {
                setTextFill(Color.GREENYELLOW);
            }
        });

        setOnMouseReleased(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent mouseEvent)
            {
                setTextFill(Color.DARKORANGE);
            }
        });

        setOnMouseEntered(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent mouseEvent)
            {
                setCursor(Cursor.HAND);
                setUnderline(true);
            }
        });

        setOnMouseExited(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent mouseEvent)
            {
                setCursor(Cursor.DEFAULT);
                setUnderline(false);
            }
        });
    }
}
