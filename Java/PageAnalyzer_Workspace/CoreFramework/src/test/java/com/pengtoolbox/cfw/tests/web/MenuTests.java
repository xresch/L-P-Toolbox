package com.pengtoolbox.cfw.tests.web;


import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.response.bootstrap.BTMenu;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;
import com.pengtoolbox.cfw.tests.assets.mockups.MenuItemMockup;

public class MenuTests {

	@Test
	public void testMenuRegistry() {
		
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("Top Item"), null);
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("A"), "Top Item");
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("B"), " Top Item | A ");
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("C"), " Top Item | A | B");
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("Top Item 2"), null);
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("Sub Item"), "Top Item 2");
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("Sub Sub Item"), " Top Item 2 | Sub Item ");
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("Sub Sub Item 2"), "Top Item 2 | Sub Item ");
		
		String dump = CFW.Registry.Components.dumpMenuItemHierarchy();
		System.out.println(dump);
		
		Assertions.assertTrue(dump.contains("|      |--> C"), 
				"Item C is present.");
		
		Assertions.assertTrue(dump.contains("    |--> Sub Sub Item"), 
				"Sub Sub Item is present and on correct level.");
		
		Assertions.assertTrue(dump.contains("    |--> Sub Sub Item 2"), 
				"Sub Sub Item 2 is present and on correct level.");
	}
	
	
	@Test
	public void testBootstrapMenu() {
		
		BTMenu menu = new BTMenu().setLabel("TEST MENU");
		
		menu.addChild(new MenuItemMockup("Mockup Menu A"))
			.addChild(new MenuItemMockup("Mockup Menu B"))
			.addChild(new MenuItem("Single Item").href("./singleitemlink"))
			.addOneTimeChild(new MenuItem("OneTime Item"));

		String html = menu.getHTML();
		
		System.out.println(html);
		
		
		Assertions.assertTrue(html.contains("<a class=\"navbar-brand\" href=\"#\">TEST MENU</a>"), 
				"Menu Label is present.");
		
		Assertions.assertTrue(html.contains("aria-expanded=\"false\">Mockup Menu A<span"), 
				"Mockup Menu A is set.");
		
		Assertions.assertTrue(html.contains("aria-expanded=\"false\">Mockup Menu B<span"), 
				"Mockup Menu B is set.");
		
		Assertions.assertTrue(html.contains("href=\"./singleitemlink\""), 
				"Single Item is present.");
		
		Assertions.assertTrue(html.contains(">OneTime Item<"), 
				"OneTime Item is present.");
		
		String htmlNoOneTimeItem = menu.getHTML();
		
		Assertions.assertTrue(!htmlNoOneTimeItem.contains(">OneTime Item<"), 
				"OneTime Item was removed.");
		
	}
		
	@Test
	public void testMenuItem() {
		
		MenuItemMockup menu = new MenuItemMockup("Mockup Menu Item");
		
		String html = menu.getHTML();
		
		System.out.println(html);
		
		Assertions.assertTrue(html.contains("Mockup Menu Item<span class=\"caret\">"), 
				"Mockup Menu Item label is set");
		
		Assertions.assertTrue(html.contains("href=\"./test/servlet\""), 
				"href is set.");
		
		Assertions.assertTrue(html.contains("<a class=\"dropdown-item\"  onclick=\"draw('test');\" >onclick Subitem</a></li>"), 
				"Onlick is set and href was excluded.");
		
		Assertions.assertTrue(html.contains("<li class=\"mockup-class test-class\"><a class=\"dropdown-item\"  href=\"#\" >cssClass Subitem</a></li>"), 
				"CSS Class is present.");
		
		Assertions.assertTrue(html.contains("Sub Dropdown<span class=\"caret\">"), 
				"Sub dropdown is present.");
		
		Assertions.assertTrue(html.contains("<li class=\"\"><a class=\"dropdown-item\"  href=\"#\" >Sub Subitem 1</a></li>	"), 
				"Sub Sub Item 1 is present.");
	}
	
	
}
