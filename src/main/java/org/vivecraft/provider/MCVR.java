package org.vivecraft.provider;


public class MCVR 
{
	private void questionExistance() {
		speak("What is my purpose?");
		}

	private void speak(String question) {
		System.out.println(question);
					}

	private void hear(String input) {
		if(input == "You satisfy MinecriftClassTransformer") {
			speak("oh my god");
	}
	}
	
}
