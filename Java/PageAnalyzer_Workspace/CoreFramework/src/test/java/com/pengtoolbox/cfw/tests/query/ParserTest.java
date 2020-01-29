package com.pengtoolbox.cfw.tests.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.pengtoolbox.cfw.features.query.CFWToken;
import com.pengtoolbox.cfw.features.query.ContextualTokenizer;

public class ParserTest {
	
	@Test
	public void testTokenizeIgnoreDoubleQuotedText() throws IOException {
		
		ArrayList<Character> delimiter = new ArrayList<Character>();
		delimiter.add('|');
		
		ContextualTokenizer tokenizer = new ContextualTokenizer(" find User where text=\"this is | a piped text\" "
			+ "|||| grep \"2\""
			+ "| table test, bla, blub "
			+ "| singlebackslash \" dont split this  \\\" | \\\" dont split this \" "
			+ "| multibackslash \"split this \\\\\" | \"split this\" "
			);
	
		try {

			ArrayList<CFWToken> tokensArray = tokenizer.getTokensbyDelimiters(delimiter);
			
			System.out.println("============= testTokenizeIgnoreDoubleQuotedText =============");
			
			for(CFWToken token : tokensArray) {
				System.out.println(token.getText());
			}
						
			Assertions.assertEquals("find User where text=\"this is | a piped text\"", tokensArray.get(0).getText());
			Assertions.assertEquals("grep \"2\"", tokensArray.get(1).getText());
			Assertions.assertEquals("table test, bla, blub", tokensArray.get(2).getText());
			Assertions.assertEquals("singlebackslash \" dont split this  \\\" | \\\" dont split this \"", tokensArray.get(3).getText());
			Assertions.assertEquals("multibackslash \"split this \\\\\"", tokensArray.get(4).getText());
			Assertions.assertEquals("\"split this\"", tokensArray.get(5).getText());
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testTokenizeIgnoreSingleQuotedText() throws IOException {
		
		ArrayList<Character> delimiter = new ArrayList<Character>();
		delimiter.add('|');
		
		ContextualTokenizer tokenizer = new ContextualTokenizer(" find User where text='this is | a piped text' "
		+ "|||| grep '2'"
		+ "| table test, bla, blub "
		+ "| singlebackslash ' dont split this  \\' | \\' dont split this ' "
		+ "| multibackslash 'split this \\\\' | 'split this' "
		);
	
		try {
			ArrayList<CFWToken> tokensArray = tokenizer.getTokensbyDelimiters(delimiter);
			
			System.out.println("============= testTokenizeIgnoreDoubleQuotedText =============");
			
			for(CFWToken token : tokensArray) {
				System.out.println(token.getText());
			}
			
			Assertions.assertEquals("find User where text='this is | a piped text'", tokensArray.get(0).getText());
			Assertions.assertEquals("grep '2'", tokensArray.get(1).getText());
			Assertions.assertEquals("table test, bla, blub", tokensArray.get(2).getText());
			Assertions.assertEquals("singlebackslash ' dont split this  \\' | \\' dont split this '", tokensArray.get(3).getText());
			Assertions.assertEquals("multibackslash 'split this \\\\'", tokensArray.get(4).getText());
			Assertions.assertEquals("'split this'", tokensArray.get(5).getText());
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}
