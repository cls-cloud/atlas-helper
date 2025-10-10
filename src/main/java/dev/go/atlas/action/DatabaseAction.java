package dev.go.atlas.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import dev.go.atlas.dialog.DatabaseDialogWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DatabaseAction extends AnAction {
    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseDialogWrapper dialog = new DatabaseDialogWrapper(e, e.getProject());
        dialog.setSize(900, 400);
        dialog.show();
    }

}
