/*
========================================================================
 Name        : AlipayTest5Document.cpp
 Author      : 
 Copyright   : Your copyright notice
 Description : 
========================================================================
*/
// [[[ begin generated region: do not modify [Generated User Includes]
#include "AlipayTest5Document.h"
#include "AlipayTest5AppUi.h"
// ]]] end generated region [Generated User Includes]

/**
 * @brief Constructs the document class for the application.
 * @param anApplication the application instance
 */
CAlipayTest5Document::CAlipayTest5Document( CEikApplication& anApplication )
	: CAknDocument( anApplication )
	{
	}

/**
 * @brief Completes the second phase of Symbian object construction. 
 * Put initialization code that could leave here.  
 */ 
void CAlipayTest5Document::ConstructL()
	{
	}
	
/**
 * Symbian OS two-phase constructor.
 *
 * Creates an instance of CAlipayTest5Document, constructs it, and
 * returns it.
 *
 * @param aApp the application instance
 * @return the new CAlipayTest5Document
 */
CAlipayTest5Document* CAlipayTest5Document::NewL( CEikApplication& aApp )
	{
	CAlipayTest5Document* self = new ( ELeave ) CAlipayTest5Document( aApp );
	CleanupStack::PushL( self );
	self->ConstructL();
	CleanupStack::Pop( self );
	return self;
	}

/**
 * @brief Creates the application UI object for this document.
 * @return the new instance
 */	
CEikAppUi* CAlipayTest5Document::CreateAppUiL()
	{
	return new ( ELeave ) CAlipayTest5AppUi;
	}
				
