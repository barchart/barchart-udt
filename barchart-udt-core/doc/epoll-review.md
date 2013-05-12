
### Interest set 

int CEPoll::add_usock(const int eid, const UDTSOCKET& u, const int* events)   
int CEPoll::remove_usock(const int eid, const UDTSOCKET& u)   
* only change presence of interest

int CEPoll::update_events(const UDTSOCKET& uid, std::set<int>& eids, int events, bool enable)
* only update result if interest present

int CEPoll::wait(const int eid, set<UDTSOCKET>* readfds, set<UDTSOCKET>* writefds, int64_t msTimeOut, set<SYSSOCKET>* lrfds, set<SYSSOCKET>* lwfds)   
* report result regardless if interest is present

```
m_mPolls(poll-id) -> m_sUDTSocksIn(socket-id)
m_mPolls(poll-id) -> m_sUDTSocksOut(socket-id)
m_mPolls(poll-id) -> m_sUDTSocksEx(socket-id)
```

### Result set

void CUDT::addEPoll(const int eid)
* add only UDT_EPOLL_IN UDT_EPOLL_OUT
* insert into result if interest is present and has matching state
   
void CUDT::removeEPoll(const int eid)   
* remove only UDT_EPOLL_IN UDT_EPOLL_OUT
* erase from result

int CEPoll::update_events(const UDTSOCKET& uid, std::set<int>& eids, int events, bool enable)
* only update result if interest present
   
int CEPoll::wait(const int eid, set<UDTSOCKET>* readfds, set<UDTSOCKET>* writefds, int64_t msTimeOut, set<SYSSOCKET>* lrfds, set<SYSSOCKET>* lwfds)   
* report result regardless if interest is present

```
m_mPolls(poll-id) -> m_sUDTReads(socket-id)
m_mPolls(poll-id) -> m_sUDTWrites(socket-id)
m_mPolls(poll-id) -> m_sUDTExcepts(socket-id)
```

### Epoll state in result

UDT_EPOLL_ERR
* is only set
* never cleared
* socket must be closed

