package com.clmcat.basics.commons.tablec;

public class SimpleTableSplit implements TableSplit {

	@Override
	public String[] getTableName(Table table) {
		if (table.getSplitLength() <= 1) {
			return new String[] {table.getName()};
		}
		String[] names = new String[table.getSplitLength()];
		for (int i = 0; i < names.length; i++) {
			names[i] = table.getName()+ "_" + i;
		}
		return names;
	}

}
