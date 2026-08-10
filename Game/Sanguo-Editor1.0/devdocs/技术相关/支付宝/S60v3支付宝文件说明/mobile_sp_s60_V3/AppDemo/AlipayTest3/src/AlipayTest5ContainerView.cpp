/*
 ========================================================================
 Name        : AlipayTest5ContainerView.cpp
 Author      : 
 Copyright   : Your copyright notice
 Description : 
 ========================================================================
 */
// [[[ begin generated region: do not modify [Generated System Includes]
#include <aknviewappui.h>
#include <eikmenub.h>
#include <avkon.hrh>
#include <barsread.h>
#include <stringloader.h>
#include <aknlists.h>
#include <eikenv.h>
#include <akniconarray.h>
#include <eikclbd.h>
#include <akncontext.h>
#include <akntitle.h>
#include <eikbtgpc.h>
#include <AlipayTest5.rsg>
#include <hash.h>
#include <e32math.h>
#include <EscapeUtils.h>
// ]]] end generated region [Generated System Includes]
// [[[ begin generated region: do not modify [Generated User Includes]
#include "AlipayTest3.hrh"
#include "AlipayTest5ContainerView.h"
#include "AlipayTest5Container.hrh"
#include "AlipayTest5Container.h"
#include "configuration.h"
//#include "StringTool.h"
#include "Alipay.h"
// ]]] end generated region [Generated User Includes]
// [[[ begin generated region: do not modify [Generated Constants]
_LIT8(KAliXPayOperationName,"alixpay");
_LIT8(KServerAddr, "http://mcashier.d131.alipay.net/uc_demo.htm?param=client&totalFee=");
_LIT(KNormalMode, "\x5168\x8BA2\x5355\x6A21\x5F0F");
_LIT(KTokenMode, "Token\x8BA2\x5355\x6A21\x5F0F");
const TInt KPayChannelId = 1234;
// ]]] end generated region [Generated Constants]

/**
 * First phase of Symbian two-phase construction. Should not contain any
 * code that could leave.
 */
CAlipayTest5ContainerView::CAlipayTest5ContainerView()
	{
	// [[[ begin generated region: do not modify [Generated Contents]
	iAlipayTest5Container = NULL;
	// ]]] end generated region [Generated Contents]

	}

/** 
 * The view's destructor removes the container from the control
 * stack and destroys it.
 */
CAlipayTest5ContainerView::~CAlipayTest5ContainerView()
	{
	// [[[ begin generated region: do not modify [Generated Contents]
	delete iAlipayTest5Container;
	iAlipayTest5Container = NULL;

	delete iInterface;
	REComSession::FinalClose();
	// ]]] end generated region [Generated Contents]

	}

/**
 * Symbian two-phase constructor.
 * This creates an instance then calls the second-phase constructor
 * without leaving the instance on the cleanup stack.
 * @return new instance of CAlipayTest5ContainerView
 */
CAlipayTest5ContainerView* CAlipayTest5ContainerView::NewL()
	{
	CAlipayTest5ContainerView* self = CAlipayTest5ContainerView::NewLC();
	CleanupStack::Pop(self);
	return self;
	}

/**
 * Symbian two-phase constructor.
 * This creates an instance, pushes it on the cleanup stack,
 * then calls the second-phase constructor.
 * @return new instance of CAlipayTest5ContainerView
 */
CAlipayTest5ContainerView* CAlipayTest5ContainerView::NewLC()
	{
	CAlipayTest5ContainerView* self = new (ELeave) CAlipayTest5ContainerView();
	CleanupStack::PushL(self);
	self->ConstructL();
	return self;
	}

/**
 * Second-phase constructor for view.  
 * Initialize contents from resource.
 */
void CAlipayTest5ContainerView::ConstructL()
	{
	// [[[ begin generated region: do not modify [Generated Code]
	BaseConstructL(R_ALIPAY_TEST5_CONTAINER_ALIPAY_TEST5_CONTAINER_VIEW);

	// ]]] end generated region [Generated Code]

	// add your own initialization code here
	iInterface = CAlixPay::NewL(KAliXPayOperationName);
	}

/**
 * @return The UID for this view
 */
TUid CAlipayTest5ContainerView::Id() const
	{
	return TUid::Uid(EAlipayTest5ContainerViewId);
	}

/**
 * Handle a command for this view (override)
 * @param aCommand command id to be handled
 */
void CAlipayTest5ContainerView::HandleCommandL(TInt aCommand)
	{
	// [[[ begin generated region: do not modify [Generated Code]
	TBool commandHandled = EFalse;
	switch (aCommand)
		{ // code to dispatch to the AknView's menu and CBA commands is generated here
		default:
			break;
		}

	if (!commandHandled)
		{
		if (aCommand == EAknSoftkeyExit)
			{
			AppUi()->HandleCommandL(EEikCmdExit);
			}
		}
	// ]]] end generated region [Generated Code]

	}

/**
 *	Handles user actions during activation of the view, 
 *	such as initializing the content.
 */
void CAlipayTest5ContainerView::DoActivateL(const TVwsViewId& /*aPrevViewId*/,
		TUid /*aCustomMessageId*/, const TDesC8& /*aCustomMessage*/)
	{
	// [[[ begin generated region: do not modify [Generated Contents]
	SetupStatusPaneL();

	if (iAlipayTest5Container == NULL)
		{
		iAlipayTest5Container = CreateContainerL();
		iAlipayTest5Container->SetMopParent(this);
		AppUi()->AddToStackL(*this, iAlipayTest5Container);

		UpdateTitle();
		}
	// ]]] end generated region [Generated Contents]

	}

/**
 */
void CAlipayTest5ContainerView::DoDeactivate()
	{
	// [[[ begin generated region: do not modify [Generated Contents]
	CleanupStatusPane();

	if (iAlipayTest5Container != NULL)
		{
		AppUi()->RemoveFromViewStack(*this, iAlipayTest5Container);
		delete iAlipayTest5Container;
		iAlipayTest5Container = NULL;
		}
	// ]]] end generated region [Generated Contents]

	}

/** 
 * Handle status pane size change for this view (override)
 */
void CAlipayTest5ContainerView::HandleStatusPaneSizeChange()
	{
	CAknView::HandleStatusPaneSizeChange();

	// this may fail, but we're not able to propagate exceptions here
	TVwsViewId view;
	AppUi()->GetActiveViewId(view);
	if (view.iViewUid == Id())
		{
		TInt result;
		TRAP( result, SetupStatusPaneL() );
		}

	// [[[ begin generated region: do not modify [Generated Code]
	// ]]] end generated region [Generated Code]

	}

// [[[ begin generated function: do not modify
void CAlipayTest5ContainerView::SetupStatusPaneL()
	{
	// reset the context pane
	TUid contextPaneUid = TUid::Uid(EEikStatusPaneUidContext);
	CEikStatusPaneBase::TPaneCapabilities subPaneContext =
			StatusPane()->PaneCapabilities(contextPaneUid);
	if (subPaneContext.IsPresent() && subPaneContext.IsAppOwned())
		{
		CAknContextPane* context =
				static_cast<CAknContextPane*> (StatusPane()->ControlL(
						contextPaneUid));
		context->SetPictureToDefaultL();
		}

	// setup the title pane
	TUid titlePaneUid = TUid::Uid(EEikStatusPaneUidTitle);
	CEikStatusPaneBase::TPaneCapabilities subPaneTitle =
			StatusPane()->PaneCapabilities(titlePaneUid);
	if (subPaneTitle.IsPresent() && subPaneTitle.IsAppOwned())
		{
		CAknTitlePane* title =
				static_cast<CAknTitlePane*> (StatusPane()->ControlL(
						titlePaneUid));
		TResourceReader reader;
		iEikonEnv->CreateResourceReaderLC(reader,
				R_ALIPAY_TEST5_CONTAINER_TITLE_RESOURCE);
		title->SetFromResourceL(reader);
		CleanupStack::PopAndDestroy(); // reader internal state
		}

	}

// ]]] end generated function

// [[[ begin generated function: do not modify
void CAlipayTest5ContainerView::CleanupStatusPane()
	{
	}

// ]]] end generated function

/**
 *	Creates the top-level container for the view.  You may modify this method's
 *	contents and the CAlipayTest5Container::NewL() signature as needed to initialize the
 *	container, but the signature for this method is fixed.
 *	@return new initialized instance of CAlipayTest5Container
 */
CAlipayTest5Container* CAlipayTest5ContainerView::CreateContainerL()
	{
	return CAlipayTest5Container::NewL(ClientRect(), NULL, this, this);
	}

void CAlipayTest5ContainerView::HandleListBoxEventL(CEikListBox* aListBox,
		TListBoxEvent aEventType)
	{
	switch (aEventType)
		{
		case MEikListBoxObserver::EEventEnterKeyPressed:
		case MEikListBoxObserver::EEventItemClicked:
			OnItemSelect(aListBox->CurrentItemIndex());
			break;
		default:
			break;
		}
	}

void CAlipayTest5ContainerView::AlipayPluginEvent(const TDesC8& resultStatus)
	{

	}

void CAlipayTest5ContainerView::OnItemSelect(TInt aPosition)
	{
	TBuf8<10> subject;
	TBuf8<10> fee;

	switch (aPosition)
		{
		case 0:
			subject.Copy(_L8("苹果"));
			fee.Copy(_L8("1"));
			break;
		case 1:
			subject.Copy(_L8("葡萄"));
			fee.Copy(_L8("50"));
			break;
		case 2:
			subject.Copy(_L8("香蕉"));
			fee.Copy(_L8("51"));
			break;
		case 3:
			subject.Copy(_L8("樱桃"));
			fee.Copy(_L8("100"));
			break;
		case 4:
			subject.Copy(_L8("芒果"));
			fee.Copy(_L8("101"));
			break;
		case 5:
			subject.Copy(_L8("西瓜"));
			fee.Copy(_L8("200"));
			break;
		case 6:
			subject.Copy(_L8("菠萝"));
			fee.Copy(_L8("201"));
			break;
		case 7:
			subject.Copy(_L8("番茄"));
			fee.Copy(_L8("500"));
			break;
		case 8:
			subject.Copy(_L8("黃桃"));
			fee.Copy(_L8("501"));
			break;
		case 9:
			subject.Copy(_L8("荔枝"));
			fee.Copy(_L8("2000"));
			break;
		case 10:
			subject.Copy(_L8("枇杷"));
			fee.Copy(_L8("2001"));
			break;
		case 11:
			subject.Copy(_L8("甘蔗"));
			fee.Copy(_L8("5000"));
			break;
		case 12:
			subject.Copy(_L8("龙眼"));
			fee.Copy(_L8("5001"));
			break;
		default:
			return;
		}

	OrderPay(subject, fee);
	}

HBufC8* CAlipayTest5ContainerView::DoMD5(const TDesC8& aString)
	{
	TBuf8<1024> string;
	string.Append(aString);
	string.Append(MD5_KEY);

	CMD5* md5 = CMD5::NewL();
	CleanupStack::PushL(md5);
	TPtrC8 hashedSig = md5->Hash(string);
	HBufC8* buf = HBufC8::NewL(hashedSig.Length() * 2);
	TPtr8 bufPtr = buf->Des();

	for (TInt i = 0; i < hashedSig.Length(); i++)
		bufPtr.AppendFormat(_L8("%+02x"), hashedSig[i]);

	CleanupStack::PopAndDestroy(md5);
	return buf;
	}

void CAlipayTest5ContainerView::GenerateKey(TDes8& aKey, TInt aLen)
	{
	aKey.Zero();

	TTime currentTime;
	currentTime.HomeTime();

	// �?000年开始计�?
	TInt startYear = 2000;

	// 当前年份
	TInt currentYear = currentTime.DateTime().Year();
	TTime time(TDateTime(currentYear, EJanuary, 0, 0, 0, 0, 0));

	TTimeIntervalSeconds s;
	currentTime.SecondsFrom(time, s);

	// 得到秒数
	TInt i = s.Int();

	aKey.AppendFormat(_L8("%X"), i);
	aKey.AppendFormat(_L8("%X"), currentYear - startYear);

	TInt len = aKey.Length();
	if (len > aLen)
		{
		aKey.Mid(0, aLen);
		}
	else
		{
		for (TInt i = 0; i < aLen - len; i++)
			{
			TTime theTime;
			theTime.UniversalTime();
			TInt64 randSeed(theTime.Int64());
			TInt number(Math::Rand(randSeed) + i);

			number = number % 10 + 48;
			aKey.Append(number);
			}
		}
	}

void CAlipayTest5ContainerView::OrderPay(const TDesC8& aSubject,
		const TDesC8& aTotalFee)
	{
	HBufC8* order = GetOrderInfo(aSubject, aTotalFee);

	if (order == NULL)
		return;

	TBuf8<2048> info;

	HBufC8* sign = NULL;
	sign = DoMD5(order->Des());

	HBufC8* signEncoded = EscapeUtils::EscapeEncodeL(sign->Des(),
			EscapeUtils::EEscapeUrlEncoded);

	info.Append(order->Des());
	info.Append(_L8("&sign=\""));
	info.Append(signEncoded->Des());
	info.Append(_L8("\""));
	info.Append(_L8("&sign_type=\"MD5\""));
	info.Append(_L8("&pay_channel_id=\""));
	info.AppendNum(KPayChannelId);
	info.Append(_L8("\""));

	delete sign;
	delete signEncoded;
	delete order;
	TRAPD(error, iInterface->Pay(REINTERPRET_CAST(CCoeAppUi*, AppUi()), this, info,0));
	}

HBufC8* CAlipayTest5ContainerView::GetOrderInfo(const TDesC8& aSubject,
		const TDesC8& aTotalFee)
	{
	TBuf8<1024> info;
	info.Append(_L8("partner=\""));
	info.Append(PartnerID);
	info.Append(_L8("\"&seller=\""));
	info.Append(SellerID);
	info.Append(_L8("\"&out_trade_no=\""));

	TBuf8<15> out_trade_no;
	GenerateKey(out_trade_no, 15);
	info.Append(out_trade_no);

	info.Append(_L8("\"&subject=\""));
	info.Append(aSubject);
	info.Append(_L8("\"&body=\"kkk\"&total_fee=\""));
	info.Append(aTotalFee);
	info.Append(_L8("\"&notify_url=\""));

	TBuf8<256> url;
	url.Copy(_L8("http://notify.java.jpxx.org/index.jsp"));
	HBufC8* urlEncoded = EscapeUtils::EscapeEncodeL(url,
			EscapeUtils::EEscapeUrlEncoded);

	info.Append(urlEncoded->Des());
	info.Append(_L8("\"&call_back_url=\""));
	info.Append(urlEncoded->Des());
	delete urlEncoded;
	info.Append(_L8("\""));

	HBufC8* buf = HBufC8::NewL(info.Length());
	buf->Des().Copy(info);

	return buf;
	}

void CAlipayTest5ContainerView::ShowMessage(const TDesC& aMessage)
	{
	CEikonEnv::Static()->InfoWinL(_L("Info"), aMessage);
	}

void CAlipayTest5ContainerView::UpdateTitle()
	{
	TUid titlePaneUid;
	titlePaneUid.iUid = EEikStatusPaneUidTitle;

	CEikStatusPane *statusPane = StatusPane();

	CEikStatusPaneBase::TPaneCapabilities subPane =
			statusPane->PaneCapabilities(titlePaneUid);

	if (subPane.IsPresent() && subPane.IsAppOwned())
		{
		CAknTitlePane* titlePane = (CAknTitlePane *) statusPane->ControlL(
				titlePaneUid);
		titlePane->SetTextL(KNormalMode);
		}
	}
