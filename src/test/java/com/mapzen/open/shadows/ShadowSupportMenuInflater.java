package com.mapzen.open.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.MenuNode;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.RoboAttributeSet;
import org.robolectric.shadows.ShadowMenuInflater;
import org.robolectric.util.I18nException;

import android.content.Context;
import android.support.v7.internal.view.SupportMenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.res.ResourceLoader.ANDROID_NS;

/**
 * Support version of {@link ShadowMenuInflater} that allows for app namespace to be used when
 * specifying {@code actionViewClass}.
 */
@Implements(SupportMenuInflater.class)
public class ShadowSupportMenuInflater extends ShadowMenuInflater {
    public static final String APP_NS = "http://schemas.android.com/apk/res/app";

    private Context context;
    private ResourceLoader resourceLoader;
    private boolean strictI18n;

    public void __constructor__(Context context) {
        this.context = context;
        resourceLoader = shadowOf(context).getResourceLoader();
        strictI18n = shadowOf(context).isStrictI18n();
    }

    @Implementation
    public void inflate(int resource, Menu root) {
        String qualifiers = shadowOf(context.getResources().getConfiguration()).getQualifiers();
        MenuNode menuNode =
                resourceLoader.getMenuNode(resourceLoader.getResourceIndex().getResName(resource),
                        qualifiers);

        try {
            addChildrenInGroup(menuNode, 0, root);
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + shadowOf(context).getResName(resource),
                    e);
        }
    }

    private void addChildrenInGroup(MenuNode source, int groupId, Menu root) {
        for (MenuNode child : source.getChildren()) {
            String name = child.getName();
            RoboAttributeSet attributes =
                    shadowOf(context).createAttributeSet(child.getAttributes(), null);
            if (strictI18n) {
                attributes.validateStrictI18n();
            }
            if (name.equals("item")) {
                if (child.isSubMenuItem()) {
                    SubMenu sub = root.addSubMenu(groupId,
                            attributes.getAttributeResourceValue(ANDROID_NS, "id", 0),
                            0, attributes.getAttributeValue(ANDROID_NS, "title"));
                    MenuNode subMenuNode = child.getChildren().get(0);
                    addChildrenInGroup(subMenuNode, groupId, sub);
                } else {
                    String menuItemTitle = attributes.getAttributeValue(ANDROID_NS, "title");
                    if (isFullyQualifiedName(menuItemTitle)) {
                        menuItemTitle = getStringResourceValue(attributes);
                    }

                    int orderInCategory =
                            attributes.getAttributeIntValue(ANDROID_NS, "orderInCategory", 0);
                    int menuItemId = attributes.getAttributeResourceValue(ANDROID_NS, "id", 0);
                    MenuItem item = root.add(groupId, menuItemId, orderInCategory, menuItemTitle);

                    addActionViewToItem(item, attributes);
                }
            } else if (name.equals("group")) {
                int newGroupId = attributes.getAttributeResourceValue(ANDROID_NS, "id", 0);
                addChildrenInGroup(child, newGroupId, root);
            }
        }
    }

    private String getStringResourceValue(RoboAttributeSet attributes) {
        int menuItemTitleId = attributes.getAttributeResourceValue(ANDROID_NS, "title", 0);
        return context.getString(menuItemTitleId);
    }

    private boolean isFullyQualifiedName(String menuItemTitle) {
        return menuItemTitle != null && menuItemTitle.startsWith("@");
    }

    private void addActionViewToItem(MenuItem item, RoboAttributeSet attributes) {
        String actionViewClassName = attributes.getAttributeValue(APP_NS, "actionViewClass");
        if (actionViewClassName != null) {
            try {
                View actionView = (View) Class.forName(actionViewClassName)
                        .getConstructor(Context.class)
                        .newInstance(context);
                item.setActionView(actionView);
            } catch (Exception e) {
                throw new RuntimeException("Action View class not found!", e);
            }
        }
    }
}
