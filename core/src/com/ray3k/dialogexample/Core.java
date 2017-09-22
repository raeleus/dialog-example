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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Core extends ApplicationAdapter {
    private Skin skin;
    private Stage stage;
    private static Core core;
    private boolean gamePaused;
    private int errorCount;
    private Array<Entity> entities;
    private Viewport gameViewport;
    private OrthographicCamera gameCamera;
    private SpriteBatch spriteBatch;
    
    @Override
    public void create() {
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        spriteBatch = new SpriteBatch();
        entities = new Array<Entity>();
        TextureAtlas textureAtlas = new TextureAtlas(Gdx.files.internal("Dialog Example.atlas"));
        TextureRegion textureRegion = textureAtlas.findRegion("player");
        entities.add(new PlayerEntity(textureRegion));
        
        gamePaused = false;
        errorCount = 0;
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
        
        float delta = Gdx.graphics.getDeltaTime();
        
        //Allow your entities to act if the game is not paused
        if (!gamePaused) {
            for (Entity entity : entities) {
                entity.act(delta);
            }
        }
        
        //Always draw your entities even if your game is paused
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        for (Entity entity : entities) {
            entity.draw(spriteBatch);
        }
        spriteBatch.end();
        
        //draw your UI on top of your entities
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        gameCamera.position.set(width / 2, height / 2.0f, 0.0f);
        
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        skin.dispose();
        stage.dispose();
    }
    
    public void showUsernameDialog() {
        errorCount = 0;
        showUsernameDialog("Please enter your username:");
    }
    
    private void showUsernameDialog(String dialogText) {
        final TextField textField = new TextField("", skin);
        
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                boolean value = (Boolean) object;
                if (value) {
                    //validate username
                    if (textField.getText().length() >= 5) {
                        showPasswordDialog();
                    } else {
                        if (errorCount < 3) {
                            showUsernameDialog("Invalid username length.\nPlease enter your username:");
                            errorCount++;
                        } else {
                            showFailureDialog("Email support\nto retrieve your username.");
                        }
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
    
    private void showPasswordDialog() {
        showPasswordDialog("Please enter your password:");
    }
    
    private void showPasswordDialog(String dialogText) {
        final TextField textField = new TextField("", skin);
        textField.setPasswordCharacter('*');
        textField.setPasswordMode(true);
        
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                boolean value = (Boolean) object;
                if (value) {
                    //validate password
                    if (textField.getText().length() >= 5) {
                        showConfirmationDialog("Correct username and password.\nPrepare to contact server...");
                    } else {
                        if (errorCount < 3) {
                            showPasswordDialog("Invalid password length.\nPlease enter your password:");
                            errorCount++;
                        } else {
                            showFailureDialog("Email support\nto reset your password.");
                        }
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
    
    private void showConfirmationDialog(String dialogText) {
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                //login to your server
                gamePaused = false;
            }
        };
        dialog.text(dialogText);
        
        dialog.button("OK");
        dialog.key(Keys.ESCAPE, null).key(Keys.ENTER, null);
        
        dialog.show(stage);
        dialog.setSize(300.0f, 200.0f);
        dialog.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f, Align.center);
    }
    
    private void showFailureDialog(String dialogText) {
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                //do something in response to multiple failed logins
                gamePaused = false;
            }
        };
        dialog.text(dialogText);
        
        dialog.button("OK");
        dialog.key(Keys.ESCAPE, null).key(Keys.ENTER, null);
        
        dialog.show(stage);
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
