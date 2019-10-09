/**
 * Sample program that reads tags on multiple readers and prints the tags found.
 * @file multireadasync.c
 */

#include <tm_reader.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <inttypes.h>
#include <string.h>
#ifndef WIN32
#include <unistd.h>
#endif

/* Enable this to use transportListener */
#ifndef USE_TRANSPORT_LISTENER
#define USE_TRANSPORT_LISTENER 0
#endif

#define usage() {errx(1, "Please provide valid reader URL, such as: reader1-uri [--ant n] reader2-uri [--ant n]\n"\
                         "reader-uri : e.g., 'tmr:///COM1' or 'tmr:///dev/ttyS0/' or 'tmr://readerIP'\n"\
                         "[--ant n] : e.g., '--ant 1'\n"\
                         "Example: 'tmr:///com4 tmr:///com13' or 'tmr:///com4 --ant 1 tmr:///com13 --ant 2' \n");}

typedef struct antennaValues
{
  uint8_t antennaBuffer[20];
  uint8_t antennaCount;
} antennaValues;

typedef struct readerDesc
{
  char uri[TMR_MAX_READER_NAME_LENGTH];
  int idx;
  TMR_ReadPlan *readPlan;
  antennaValues _antStorage;
} readerDesc;

void errx(int exitval, const char *fmt, ...)
{
  va_list ap;

  va_start(ap, fmt);
  vfprintf(stderr, fmt, ap);

  exit(exitval);
}

void checkerr(TMR_Reader* rp, TMR_Status ret, int exitval, const char *msg)
{
  if (TMR_SUCCESS != ret)
  {
    errx(exitval, "Error %s: %s\n", msg, TMR_strerr(rp, ret));
  }
}

void serialPrinter(bool tx, uint32_t dataLen, const uint8_t data[],
                   uint32_t timeout, void *cookie)
{
  FILE *out = stdout;
  readerDesc *rdp = cookie;
  uint32_t i;

  fprintf(out, "%s %s", rdp->uri, tx ? "Sending: " : "Received:");
  for (i = 0; i < dataLen; i++)
  {
    if (i > 0 && (i & 15) == 0)
      fprintf(out, "\n         ");
    fprintf(out, " %02x", data[i]);
  }
  fprintf(out, "\n");
}

void stringPrinter(bool tx,uint32_t dataLen, const uint8_t data[],uint32_t timeout, void *cookie)
{
  FILE *out = stdout;

  fprintf(out, "%s", tx ? "Sending: " : "Received:");
  fprintf(out, "%s\n", data);
}

void parseAntennaList(uint8_t *antenna, uint8_t *antennaCount, char *args)
{
  char *token = NULL;
  char *delimiters = ",";
  uint8_t i = 0;
  int scans;

  /* get the first token */
  if (NULL == args)
  {
    fprintf(stdout, "Missing argument\n");
    usage();
  }

  token = strtok(args, delimiters);
  if (NULL == token)
  {
    fprintf(stdout, "Can't parse '%s' as comma-separated list\n", args);
    usage();
  }

  while(NULL != token)
  {
    scans = sscanf(token, "%"SCNu8, &antenna[i]);
    if (1 != scans)
    {
      fprintf(stdout, "Can't parse '%s' as an 8-bit unsigned integer value\n", token);
      usage();
    }
    i++;
    token = strtok(NULL, delimiters);
  }
  *antennaCount = i;
}

void callback(TMR_Reader *reader, const TMR_TagReadData *t, void *cookie);
void exceptionCallback(TMR_Reader *reader, TMR_Status error, void *cookie);

int main(int argc, char *argv[])
{

#ifndef TMR_ENABLE_BACKGROUND_READS
  errx(1, "This sample requires background read functionality.\n"
          "Please enable TMR_ENABLE_BACKGROUND_READS in tm_config.h\n"
          "to run this codelet\n");
  return -1;
#else

  TMR_Reader *r;
  readerDesc *rd;
  int rcount;
  TMR_Reader *rp;
  TMR_Status ret;
  TMR_Region region;
  TMR_ReadListenerBlock *rlb;
  TMR_ReadExceptionListenerBlock *reb;
  char *readerName = NULL;
  uint8_t *antennaList = NULL;
#if USE_TRANSPORT_LISTENER
  TMR_TransportListenerBlock *tb;
#endif
  int i;

  if (argc < 2)
  {
    usage();
  }
  
  /* this is a non-optimized, worst-case estimate. */
  rcount = argc-1;
  r = (TMR_Reader*) calloc(rcount, sizeof(TMR_Reader));
  rd = (readerDesc*) calloc(rcount, sizeof(readerDesc));
  rlb = (TMR_ReadListenerBlock*) calloc(rcount, sizeof(TMR_ReadListenerBlock));
  reb = (TMR_ReadExceptionListenerBlock*) calloc(rcount, sizeof(TMR_ReadExceptionListenerBlock));
  
  rd->idx = 0;
  for (i = 1; i < argc; i++)
  {
    if(0 == strcmp("--ant", argv[i]))
    {
      /* Its a antenna list */
      if (NULL != readerName)
      {
        if (NULL != antennaList)
        {
          fprintf(stdout, "Duplicate argument: --ant specified more than once\n");
          usage();
        }
        parseAntennaList(rd[rd->idx -1]._antStorage.antennaBuffer, &rd[rd->idx -1]._antStorage.antennaCount, argv[i+1]);
        i++;
        antennaList = rd[rd->idx -1]._antStorage.antennaBuffer;
      }
      else
      {
        usage();
      }
    }
    else
    {
      /* Its a reader name */      
      strcpy(rd[rd->idx].uri, argv[i]);
      readerName = rd[rd->idx].uri;
      rd->idx++;
      antennaList = NULL;
    }
  }

  for (i = 0; i < rd->idx; i++)
  {
    rp = &r[i];
    ret = TMR_create(rp, rd[i].uri);
    checkerr(rp, ret, 1, "creating reader %s");

    printf("Created reader %d: %s\n", i+1, rd[i].uri);

#if USE_TRANSPORT_LISTENER

    tb = (TMR_TransportListenerBlock*) calloc(rcount, sizeof(TMR_TransportListenerBlock));

    if (TMR_READER_TYPE_SERIAL == rp->readerType)
    {
      tb[i].listener = serialPrinter;
    }
    else
    {
      tb[i].listener = stringPrinter;
    }
    tb[i].cookie = &rd[i];

    TMR_addTransportListener(rp, &tb[i]);
#endif


    //TMR_SR_PowerMode pm = TMR_SR_POWER_MODE_FULL;
    //ret = TMR_paramSet(rp, TMR_PARAM_POWERMODE, &pm);

    ret = TMR_connect(rp);
    checkerr(rp, ret, 1, "connecting reader");

    region = TMR_REGION_NONE;
    ret = TMR_paramGet(rp, TMR_PARAM_REGION_ID, &region);
    checkerr(rp, ret, 1, "getting region");

    if (TMR_REGION_NONE == region)
    {
      TMR_RegionList regions;
      TMR_Region _regionStore[32];
      regions.list = _regionStore;
      regions.max = sizeof(_regionStore)/sizeof(_regionStore[0]);
      regions.len = 0;

      ret = TMR_paramGet(rp, TMR_PARAM_REGION_SUPPORTEDREGIONS, &regions);
      checkerr(rp, ret, __LINE__, "getting supported regions");

      if (regions.len < 1)
      {
        checkerr(rp, TMR_ERROR_INVALID_REGION, __LINE__, "Reader doesn't support any regions");
      }
      region = regions.list[0];
      ret = TMR_paramSet(rp, TMR_PARAM_REGION_ID, &region);
      checkerr(rp, ret, 1, "setting region");  
    }
  /**
   * Checking the software version of the sargas.
   * The antenna detection is supported on sargas from software version of 5.3.x.x.
   * If the Sargas software version is 5.1.x.x then antenna detection is not supported.
   * User has to pass the antenna as arguments.
   */
  {
    ret = isAntDetectEnabled(rp, &(rd[i]._antStorage.antennaCount));
    if(TMR_ERROR_UNSUPPORTED == ret)
    {
      fprintf(stdout, "Reader doesn't support antenna detection. Please provide antenna list.\n");
      usage();
    }
    else
    {
      checkerr(rp, ret, 1, "Getting Antenna Detection Flag Status");
    }
  }
    /**
    * for antenna configuration we need two parameters
    * 1. antennaCount : specifies the no of antennas should
    *    be included in the read plan, out of the provided antenna list.
    * 2. antennaList  : specifies  a list of antennas for the read plan.
    **/ 

    // initialize the read plan 
    rd[i].readPlan = (TMR_ReadPlan*) malloc(sizeof(TMR_ReadPlan));
    if (rd[i]._antStorage.antennaCount)
    {
      ret = TMR_RP_init_simple(rd[i].readPlan, rd[i]._antStorage.antennaCount, rd[i]._antStorage.antennaBuffer, TMR_TAG_PROTOCOL_GEN2, 1000);
      checkerr(rp, ret, 1, "initializing the  read plan");
    }
    else
    {
      ret = TMR_RP_init_simple(rd[i].readPlan, rd[i]._antStorage.antennaCount, NULL, TMR_TAG_PROTOCOL_GEN2, 1000);
      checkerr(rp, ret, 1, "initializing the  read plan");
    }
    

    /* Commit read plan */
    ret = TMR_paramSet(rp, TMR_PARAM_READ_PLAN, rd[i].readPlan);
    checkerr(rp, ret, 1, "setting read plan");

    rlb[i].listener = callback;
    rlb[i].cookie = &rd[i];

    reb[i].listener = exceptionCallback;
    reb[i].cookie = NULL;

    ret = TMR_addReadListener(rp, &rlb[i]);
    checkerr(rp, ret, 1, "adding read listener");

    ret = TMR_addReadExceptionListener(rp, &reb[i]);
    checkerr(rp, ret, 1, "adding exception listener");

    ret = TMR_startReading(rp);
    checkerr(rp, ret, 1, "starting reading");
  }

#ifndef WIN32
  sleep(5);
#else
  Sleep(5000);
#endif

  for (i=0; i<rd->idx; i++)
  {
    rp = &r[i];
    ret = TMR_stopReading(rp);
    checkerr(rp, ret, 1, "stopping reading");
    TMR_destroy(rp);
  }
  return 0;

#endif /* TMR_ENABLE_BACKGROUND_READS */
}


void
callback(TMR_Reader *reader, const TMR_TagReadData *t, void *cookie)
{
  char epcStr[128];
  readerDesc *rdp = cookie;

  TMR_bytesToHex(t->tag.epc, t->tag.epcByteCount, epcStr);
  printf("%s: %s\n", rdp->uri, epcStr);
}

void
exceptionCallback(TMR_Reader *reader, TMR_Status error, void *cookie)
{
  fprintf(stdout, "Error:%s\n", TMR_strerr(reader, error));
}
