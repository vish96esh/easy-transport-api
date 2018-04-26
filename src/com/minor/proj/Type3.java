package com.minor.proj;

public class Type3 {
	int from[];
	int bus[];
	int no_of_stops[];
	int flag[];
	int top;
	int last_stop;
	public Type3()
	{
		last_stop=0;
		from=new int[5];
		bus=new int[5];
		no_of_stops=new int[5];
		top=-1;
		flag=new int[8];
		for(int i=0;i<8;i++)
			flag[i]=0;
	}
	
	public Type3(Type3 obj)
	{
		last_stop=obj.last_stop;
		from=new int[5];
		bus=new int[5];
		no_of_stops=new int[5];
		flag=new int[8];
		for(int i=0;i<5;i++)
		{
			from[i]=obj.from[i];
			bus[i]=obj.bus[i];
			no_of_stops[i]=obj.no_of_stops[i];
		}
		for(int i=0;i<8;i++)
			flag[i]=obj.flag[i];
		top=obj.top;
	}
	
	public void insert(int f, int b, int n)
	{
		top++;
		if(top>=5)
			return;
		from[top]=f;
		bus[top]=b;
		no_of_stops[top]=n;
	}
}
