/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.dialogexample;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Core extends ApplicationAdapter {
    private Skin skin;
    private Stage stage;
    private static Core core;
    private boolean gamePaused;
    
    @Override
    public void create() {
        gamePaused = false;
        core = this;
        InputManager inputManager = new InputManager();
        
        skin = new Skin(Gdx.files.internal("Dialog Example.json"));
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, inputManager));
        
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Label label = new Label("Press R to login", skin, "red");
        root.add(label).expand().top().left().pad(20.0f);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        skin.dispose();
        stage.dispose();
    }
    
    public void showUsernameDialog() {
        showUsernameDialog("Please enter your username:");
    }
    
    public void showUsernameDialog(String dialogText) {
        final TextField textField = new TextField("", skin);
        
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                boolean value = (Boolean) object;
                if (value) {
                    //validate username
                    if (textField.getText().length() >= 5) {
                        //show password dialog
                    } else {
                        showUsernameDialog("Invalid username length.\nPlease enter your username:");
                    }
                } else {
                    gamePaused = false;
                }
            }
        };
        dialog.text(dialogText);
        
        dialog.getContentTable().row();
        dialog.getContentTable().add(textField);
        
        dialog.button("OK", true).button("Cancel", false);
        dialog.key(Keys.ESCAPE, false).key(Keys.ENTER, true);
        
        dialog.show(stage, sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade), new Action() {
            @Override
            public boolean act(float delta) {
                stage.setKeyboardFocus(textField);
                return true;
            }
        }));
        dialog.setSize(300.0f, 200.0f);
        dialog.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f, Align.center);
    }

    public static Core getCore() {
        return core;
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    public void setGamePaused(boolean gamePaused) {
        this.gamePaused = gamePaused;
    }
}
