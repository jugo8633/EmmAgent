#include "process.h"
#include "pmonitor.h"

int work_ps_count = 0;
process_info *work_ps_list;

long ps_list_count()
{
	return work_ps_count;
}

int ps_dump()
{
	//__android_log_print(ANDROID_LOG_INFO, "JNIMsg", "run JNI function:  %s ",	"ps_dump");

	ps_list_empty(&work_ps_list);

	DIR *d;
	struct dirent *de;
	char *namefilter = 0;
	int pidfilter = 0;
	int threads = 0;

	d = opendir("/proc");
	if (d == 0)
		return;

	while ((de = readdir(d)) != 0)
	{
		if (isdigit(de->d_name[0]))
		{
			int pid = atoi(de->d_name);
			ps_instance_dump(pid);
		}
	}
	closedir(d);
	return work_ps_count;
}

void ps_list_add(process_info *new_ps)
{
	//__android_log_print(ANDROID_LOG_INFO, "JNIMsg", "run JNI function:  %s ",	"ps_list_add");

	process_info *new_node = (process_info *) malloc(sizeof(process_info));

	memcpy(new_node, new_ps, sizeof(process_info));
	new_node->next = (void *) 0;

	// find node
	if (work_ps_list != (void *) 0)
	{
		process_info *end = work_ps_list;
		while (end->next != (void *) 0)
			end = end->next;
		end->next = new_node;
	}
	else
	{
		work_ps_list = new_node;
	}

	++work_ps_count;
}

void ps_instance_dump(int pid)
{
	//__android_log_print(ANDROID_LOG_INFO, "JNIMsg", "run JNI function:  %s ",	"ps_instance_dump");

	char statline[BUFFERSIZE * 2];
	struct stat stats;
	process_info psinfo;
	FILE *ps;
	struct passwd *pw;
	int n;

	memset(&psinfo, 0, sizeof(process_info));

	psinfo.pid = pid;

	snprintf(statline, BUFFERSIZE * 2, "/proc/%d", pid);
	stat(statline, &stats);

	psinfo.uid = (int) stats.st_uid;

	pw = getpwuid(stats.st_uid);
	if (pw == 0)
	{
		snprintf(psinfo.owner, 80, "%d", (int) stats.st_uid);
	}
	else
	{
		strncpy(psinfo.owner, pw->pw_name, 80);
	}

	snprintf(statline, BUFFERSIZE * 2, "/proc/%d/stat", pid);
	ps = fopen(statline, "r");
	if (ps != 0)
	{

		/* Scan rest of string. */
		fscanf(ps, "%*d %*s %c %*d %*d %*d %*d %*d %*d %*d %*d %*d %*d "
				"%lu %lu %*d %*d %*d %*d %d %*d %*d %*lu %ld", &psinfo.status,
				&psinfo.delta_utime, &psinfo.delta_stime, &psinfo.threadnum,
				&psinfo.rss);

		fclose(ps);
	}

	snprintf(statline, BUFFERSIZE * 2, "/proc/%d/cmdline", pid);
	ps = fopen(statline, "r");
	if (ps == 0)
	{
		n = 0;
	}
	else
	{
		n = fread(psinfo.name, 1, 80, ps);
		fclose(ps);
		if (n < 0)
			n = 0;
	}
	psinfo.name[n] = 0;

	if (strlen(psinfo.name) == 0)
	{
		snprintf(statline, BUFFERSIZE * 2, "/proc/%d/stat", pid);
		ps = fopen(statline, "r");
		if (ps != 0)
		{
			n = fscanf(ps, "%d (%s)", &pid, psinfo.name);
			fclose(ps);
			if (n == 2)
				psinfo.name[strlen(psinfo.name) - 1] = 0;
		}
	}

	if ((strcmp(psinfo.owner, "root") != 0)
			&& (strstr(psinfo.name, "/system/") == 0)
			&& (strstr(psinfo.name, "/sbin/") == 0)
			&& (strcmp(psinfo.name, "sh") != 0)
			&& (strcmp(psinfo.name, "logcat") != 0)
			&& (strcmp(psinfo.name, "watchdog") != 0)
			&& (strcmp(psinfo.name, "sleep") != 0)
			&& (strcmp(psinfo.name, "system_server") != 0))
		ps_list_add(&psinfo);
}

void ps_list_empty(process_info **work_ps_list)
{
	if (*work_ps_list == (void *) 0)
		return;

	// reset
	process_info *old_node = (process_info *) *work_ps_list;
	*work_ps_list = (void *) 0;

	// release memory
	process_info *next_node = (void *) 0;
	while (old_node->next != (void *) 0)
	{
		next_node = (process_info *) old_node->next;
		free(old_node);
		old_node = next_node;
	}
	free(old_node);
	work_ps_count = 0;
	__android_log_print(ANDROID_LOG_INFO, "JNIMsg", "run JNI function:  %s ",
			"ps_list_empty");
	return;
}

void ps_release()
{
	ps_list_empty(&work_ps_list);
}

void ps_get_name(int position, char *buf)
{
	if (work_ps_count < position || position < 0)
	{
		buf[0] = 0;
		return;
	}

	process_info *current;
	if (ps_list_setposition(&work_ps_list, &current, position) != 0)
		strncpy(buf, current->name, BUFFERSIZE);
	else
		buf[0] = 0;
	return;
}

void ps_list_reset(process_info **work_ptr)
{
	*work_ptr = (void *) 0;
}

int ps_list_setposition(process_info **work_list, process_info **work_ptr,
		int position)
{
	if (position == -1)
		return 1;

	ps_list_reset(work_ptr);
	while (position >= 0)
	{
		if (!ps_list_nextrecord(work_list, work_ptr))
			return 0;
		--position;
	}
	return 1;
}

int ps_list_nextrecord(process_info **work_list, process_info **work_ptr)
{
	if (*work_ptr == (void *) 0)
		*work_ptr = *work_list;
	else
		*work_ptr = (*work_ptr)->next;

	if ((*work_ptr) == (void *) 0)
		return 0;
	return 1;
}

void ps_get_status(int position, char *buf)
{
	if (work_ps_count < position || position < 0)
	{
		buf[0] = 0;
		return;
	}

	process_info *current;
	if (ps_list_setposition(&work_ps_list, &current, position) != 0)
		snprintf(buf, BUFFERSIZE, "%c", current->status);
	else
		snprintf(buf, BUFFERSIZE, "%c", 0);

	return;
}

int ps_info(int position, char *bufName, char *bufStatus)
{
	bufName[0] = 0;
	bufStatus[0] = 0;

	if (work_ps_count < position || position < 0)
	{
		return 0;
	}

	process_info *current;
	if (ps_list_setposition(&work_ps_list, &current, position) != 0)
	{
		strncpy(bufName, current->name, BUFFERSIZE);
		snprintf(bufStatus, BUFFERSIZE, "%c", current->status);
	}

	return 1;
}
