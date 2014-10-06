typedef struct processinfo_proto
{
	char name[80];
	char owner[80];
	char status;
	unsigned int uid, pid, load, threadnum;
	unsigned long rss;
	unsigned long delta_utime;
	unsigned long delta_stime;
	double delta_load;
	void *next;
} process_info;

void ps_instance_dump(int pid);
long ps_list_count();
int ps_dump();
void ps_list_add(process_info *new_ps);
void ps_list_empty(process_info **work_ps_list);
void ps_system_add(process_info *new_ps);
void ps_release();
void ps_get_name(int position, char *buf);
int ps_list_setposition(process_info **work_list, process_info **work_ptr,
		int position);
void ps_list_reset(process_info **work_ptr);
int ps_list_nextrecord(process_info **work_list, process_info **work_ptr);
void ps_get_status(int position, char *buf);
int ps_info(int position, char *bufName, char *bufStatus);

