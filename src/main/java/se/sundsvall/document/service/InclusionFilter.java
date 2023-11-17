package se.sundsvall.document.service;

import java.util.List;

public enum InclusionFilter {
	CONFIDENTIAL_AND_PUBLIC(List.of(true, false)),
	PUBLIC(List.of(false));

	private List<Boolean> value;

	private InclusionFilter(List<Boolean> value) {
		this.value = value;
	}

	public List<Boolean> getValue() {
		return value;
	}
}
