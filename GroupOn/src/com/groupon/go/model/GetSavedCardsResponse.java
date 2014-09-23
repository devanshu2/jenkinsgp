package com.groupon.go.model;

import java.util.ArrayList;

public class GetSavedCardsResponse extends CommonJsonResponse {

	private GetSavedCardsResult	result;

	/**
	 * @return the result
	 */
	public GetSavedCardsResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(GetSavedCardsResult result) {
		this.result = result;
	}

	public class GetSavedCardsResult {

		private ArrayList<SavedCardsModel>	cards;

		/**
		 * @return the cards
		 */
		public ArrayList<SavedCardsModel> getCards() {
			return cards;
		}

		/**
		 * @param cards
		 *            the cards to set
		 */
		public void setCards(ArrayList<SavedCardsModel> cards) {
			this.cards = cards;
		}
	}

}
