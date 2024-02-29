/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023 TESLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.teslauncher.gui.accountsview;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.Account;
import me.theentropyshard.teslauncher.gui.playview.PlayViewHeader;
import me.theentropyshard.teslauncher.utils.SwingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AccountItem extends JPanel {
    private final Account account;
    private final JLabel nickLabel;
    protected Color defaultColor;
    protected Color hoveredColor;
    protected Color pressedColor;
    private Color borderColor;

    private final Set<ActionListener> mouseClickListeners;
    private final Image trashIcon;

    protected boolean mouseOver;
    protected boolean mousePressed;

    private boolean selected;

    protected final int border = 12;

    private final Rectangle trashBounds;

    public AccountItem(Account account) {
        super(new BorderLayout());
        this.account = account;

        this.mouseClickListeners = new HashSet<>();
        this.trashIcon = SwingUtils.getImage("/trash_icon.png");
        this.trashBounds = new Rectangle(0, 0, 32, 32);

        this.borderColor = UIManager.getColor("AccountItem.borderColor");

        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        FlowLayout layout1 = new FlowLayout(FlowLayout.LEFT);
        layout1.setHgap(0);
        layout1.setVgap(0);
        JPanel leftPanel = new JPanel(layout1);
        leftPanel.setOpaque(false);
        JLabel headIcon = new JLabel(SwingUtils.loadIconFromBase64(account.getHeadIcon()));
        leftPanel.add(headIcon);
        this.nickLabel = new JLabel(account.getUsername());
        this.nickLabel.setBorder(new EmptyBorder(0, 6, 0, 0));
        leftPanel.add(this.nickLabel);

        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        layout.setHgap(0);
        layout.setVgap(0);
        JPanel rightPanel = new JPanel(layout);
        rightPanel.setOpaque(false);

        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.EAST);

        this.setOpaque(false);
        this.setBorder(new EmptyBorder(
                this.border,
                this.border,
                this.border,
                this.border
        ));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                AccountItem.this.mouseOver = true;
                AccountItem.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                AccountItem.this.mouseOver = false;
                AccountItem.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                AccountItem.this.mousePressed = true;
                AccountItem.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                AccountItem.this.mousePressed = false;
                AccountItem.this.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (AccountItem.this.trashBounds.contains(e.getPoint())) {
                    int option = JOptionPane.showConfirmDialog(
                            TESLauncher.window.getFrame(),
                            "Are you sure that you want to remove account '" + account.getUsername() + "'?",
                            "Account removal",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (option != JOptionPane.OK_OPTION) {
                        return;
                    }

                    try {
                        TESLauncher.getInstance().getAccountsManager().removeAccount(account);
                    } catch (IOException ex) {
                        ex.printStackTrace();

                        JOptionPane.showMessageDialog(
                                TESLauncher.window.getFrame(),
                                "Unable to remove account '" + account.getUsername() + "': " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );

                        return;
                    }

                    TESLauncher.getInstance().getGui().getAccountsView().removeAccountItem(AccountItem.this);

                    if (account.isSelected()) {
                        PlayViewHeader header = TESLauncher.getInstance().getGui().getPlayView().getHeader();
                        header.setCurrentAccount(null);
                    }
                } else {
                    ActionEvent event = new ActionEvent(AccountItem.this, 1, String.valueOf(e.getButton()));
                    AccountItem.this.mouseClickListeners.forEach(listener -> listener.actionPerformed(event));
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color color = this.defaultColor;

        if (this.mouseOver) {
            color = this.hoveredColor;
        }

        if (this.mousePressed) {
            color = this.pressedColor;
        }

        g.setColor(color);
        g.fillRoundRect(
                this.border / 4,
                this.border / 4,
                this.getWidth() - this.border / 2,
                this.getHeight() - this.border / 2,
                10, 10
        );

        if (this.mouseOver) {
            Dimension size = this.getSize();

            int iw = 32;
            int ih = 32;

            int x = size.width - this.border - iw;
            int y = size.height / 2 - ih / 2;

            this.trashBounds.x = x;
            this.trashBounds.y = y;

            g.drawImage(this.trashIcon, x, y, null);
        }

        if (this.selected) {
            g.setColor(this.borderColor);
            ((Graphics2D) g).setStroke(new BasicStroke(2));
            g.drawRoundRect(this.border / 4,
                    this.border / 4,
                    this.getWidth() - this.border / 2,
                    this.getHeight() - this.border / 2,
                    10, 10);
        }

        super.paintComponent(g);
    }

    public void updateColors() {
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));
        this.borderColor = UIManager.getColor("AccountItem.borderColor");
    }

    public Account getAccount() {
        return this.account;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setHoveredColor(Color hoveredColor) {
        this.hoveredColor = hoveredColor;
    }

    public void setPressedColor(Color pressedColor) {
        this.pressedColor = pressedColor;
    }

    public void addMouseClickListener(ActionListener listener) {
        this.mouseClickListeners.add(listener);
    }

    public void removeMouseClickListener(ActionListener listener) {
        this.mouseClickListeners.remove(listener);
    }

    public JLabel getNickLabel() {
        return this.nickLabel;
    }
}
