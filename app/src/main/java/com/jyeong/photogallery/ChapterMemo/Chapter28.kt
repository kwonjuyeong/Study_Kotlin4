package com.jyeong.photogallery.ChapterMemo

/*
Chapter 28
브로드캐스트 인텐트

목표
- 앱이 실행 중일 때 새로운 사진에 관해 알림이 나타나지 않게 폴링 기능을 개선한다.
- 개선 작업을 통해 브로드캐스트 인텐트(broadcast intent)를 리스닝하는 방법과 브로드캐스트 수신자(broadcast receiver)를 사용해서
브로드캐스트 인텐트를 처리하는 방법에 대해 학습한다.
- 런타임 시에 앱에서 동적으로 브로드캐스트 인텐트를 전송하거나 수신하고, 앱이 현재 포그라운드에서 실행 중인지를 결정하기 위해 순차 브로드캐스트를 사용한다.

일반 인텐트 vs 브로드캐스트 인텐트
- 안드로이드 장치에서 여러 종류의 이벤트들에 반응할 수 있도록 해당 이벤트들을 알아야 하는 컴포넌트들이 시스템에 많이 존재할 수 있다
- 이 때 안드로이드는 브로드캐스트 인텐트를 사용해서 컴포넌트에 알려준다.
- 시스템이 전송하는 브로드캐스트 인텐트를 시스템 브로드캐스트 인텐트라고 한다.
- 커스텀 브로드캐스트 인텐트를 전송하거나 수신할 수도 있다.
- 시스템 브로드캐스트 인텐트와 커스텀 브로드캐스트 인텐트 모두 인텐트를 수신하는 매커니즘은 같다.

브로드 캐스트 인텐트
- 브로드캐스트 인텐트는 이미 알고 있는 인텐트와 유사하게 작동한다.
- 차이점이라면 브로드캐스트 인텐트는 브로드캐스트 수신자로 등록한 다수의 컴포넌트가 동시에 받을 수 있다는 점이다.

        인텐트                 브로드캐스트 인텐트
       컴포넌트                      컴포넌트
    ActivityManager           ActivityManager
     다른 컴포넌트          다른 컴포넌트     다른 컴포넌트
                    다른 컴포넌트    다른 컴포넌트    다른 컴포넌트

- 일반 인텐트에서는 액티비티나 서비스가 외부에 공개된 API의 일부로 사용되면 언제든지 암시적 인텐트에 응답할 수 있으며,
외부에 공개되지 않는 API의 일부이면 명시적 인텐트에 응답할 수 있다.
- 이와는 달리, 브로드캐스트 인텐트는 브로드캐스트 수신자로 등록된 다수의 액티비티나 서비스가 동시에 받고 응답할 수 있다
- 브로드캐스트 수신자도 명시적 인텐트에 응답할 수 있지만, 명시적 인텐트는 하나의 수신자만 가져서 거의 사용되지 않는다.

포그라운드 알림 차단하기
- 사용자가 이미 앱을 실행해 화면을 보고 있는 사태(포그라운드)인데도 여전히 알림이 전달되는데 브로드캐스트 인텐트를 사용해서 PollWorker의 작동을 변경할 수 있다.
- 플리커 사이트에서 새로운 사진을 가져올 때마다 PollWorker에서 브로드캐스트 인텐트를 전송하고 두 개의 브로드캐스트 수신자를 등록하게 한다.
- 첫 번째 수신자는 안드로이드 매니페스트에 등록하며, 이 수신자가 PollWorker로부터 브로드캐스트 인텐트를 받을 때마다 이전에 했던 대로 사용자에게 알림을 게시한다.
- 두 번째 수신자는 앱의 화면을 사용자가 볼 수 있을 때만 활성화되도록 동적으로 등록한다.
- 이 수신자는 매니페스트에 등록된 브로드캐스트 수신자에게 전달되는 브로드캐스트 인텐트를 가로채서 알림을 게시하지 못하게 한다.

브로드캐스트 인텐트 전송하기
- 새로운 검색 결과를 게시할 준비가 되었음을 관심 있는 컴포넌트에 알리는 브로드캐스트 인텐트를 전송한다.
- 인텐트를 생성해서 sendBroadcast(Intent)의 인자로 전달한다.

브로드캐스트 수신자 생성과 등록하기
- 브로드캐스트 인텐트가 전송되도 이것을 리스닝 하는 것이 없다.
- 따라서 브로드캐스트 인텐트에 반응하기 위해 BroadcatReceiver의 서브 클래스를 구현한다.
- 브로드캐스트 수신자에는 두 종류가 있다, 여기서는 독립 실행형(standalone) 브로드캐스트 수신자를 사용한다.
- 독립 실행형 브로드캐스트 수신자는 매니페스트에 선언된 수신자이며, 이런 수신자는 앱이 종료되더라도 활성화할 수 있다.
- 동적 브로드캐스트는 액티비티나 프래그먼트와 같은 가시적인 앱 컴포넌트의 생명주기와 연관되는 브로드캐스트 수신자다.
- 서비스나 액티비티처럼 브로드캐스트 수신자는 시스템에 등록되어야 한다.
- NotificationReceiver 서브 클래스에 인텐트가 요청되면 이것의 onReceive 함수가 자동 호출된다.
Android Manifest
<receiver android:name=".NotificationReceiver">
    <intent-filter>
        <action
            android:name="com.jyeong.photogallery.SHOW_NOTIFICATION"/>
    </intent-filter>
</receiver>

private 퍼미션을 사용해서 브로드캐스트 인텐트를 우리 앱으로 제한하기
- 브로드캐스트 인텐트는 시스템의 어떤 컴포넌트도 리스닝할 수 있고 우리 수신자를 작동시킬 수도 있다는 문제점이 있다.
- 이때 브로드캐스트 인텐트와 수신자에 퍼미션을 지정하면 이 문제를 해결할 수 있으며, 더 최신 버전의 안드로이드에서도 브래드캐스트 수신자가 작동하게 된다.
<permission android:name="android.jyeong.photogallery.PRIVATE"
        android:protectionLevel="signature"/>
<uses-permission android:name="com.jyeong.photogallery.PRIVATE"/>
- 여기서는 보호 수준(protection level)을 "signature"로 갖는 커스텀 퍼미션을 정의한다.

보호 수준
- 모든 커스텀 퍼미션에는 android:protectionLevel의 값을 지정해야 하며, 보호 수준을 나타내는 protectionLevel 속성은 퍼미션을 어떻게 사용할 것인지 안드로이드에 알려준다.

protectionLevel의 값
1. normal
사적인 데이터를 사용하거나 지도상의 위치를 찾는 것과 같은 어떤 위험한 일도 하지 않도록 앱 기능을 보호하기 위한 것이다.
앱의 설치를 선택하기 전에 사용자는 해당 퍼미션을 볼 수 있지만, 사용자의 승인은 받지 않는다.
android:permission.INTERNET은 이 퍼미션 수준을 사용하며, 인터넷을 사용하게 해준다.
2. dangerous
normal로 사용하지 않는 모든 것들의 보호 수준이다. 예를 들면, 사적인 데이터의 사용, 사용자를 염탐하는데 사용될 수 있는 하드웨어 사용,
사용자에게 문제를 초래할 수 있는 그 밖의 것들이다. 카메라 퍼미션, 위치 퍼미션, 연락처 퍼미션 모두 이 유형에 속한다.
dangerous 퍼미션의 경우 안드로이드 6.0부터는 런타임 시에 requestPermission을 호출해서 사용자에게 앱 퍼미션을 승인받아야 한다.
3. signature
이 퍼미션을 선언하고 있는 앱과 같은 인증 키로 다른 앱이 서명되어 있다면 시스템은 이 퍼미션을 승인하며, 그렇지 않으면 거부한다.
이 퍼미션이 승인되어도 사용자에게는 알리지 않는다. 앱에서 내부적으로 사용하는 퍼미션으로, 앱에서 인증을 갖고 있고,
동일한 인증으로 서명된 앱들만이 이 퍼미션을 사용할 수 있으므로 이 퍼미션의 사용을 직접 통제할 수 있다.
여기서는 우리 브로드캐스트 인텐트를 다른 앱에서 받지 못하게 하기 위해 사용한다.
4. signatureOrSystem
signature 보호 수준과 같지만, 안드로이드 시스템 이미지의 모든 패키지에도 퍼미션을 승인한다.
시스템 이미지에 내장된 앱들과 소통하는 데 사용되며, 이 퍼미션이 승인될 떄는 사용자에게 알리지 않는다.
주로 하드웨어 제조사에서 사용하기 위한 보호수준이므로 대부분의 개발자느느 이 보호 수준을 사용할 필요가 없다.

동적 수신자를 생성하고 등록하기
- ACTION_SHOW_NOTIFICATION 브래드캐스트 인텐트의 수신자는 사용자가 이 앱을 사용하는 동안 알림이 게시되는 것을 막는다.
- 이 수신자는 액티비티가 포그라운드에 있을 떄만 등록하게 되며, 만일 이 앱의 프로세스 생애처럼 이 수신자가 더 긴 수명을 갖도록 선언된다면 PhotoGalleryFragment가 실행 중인지 알 방법이 필요하다.
- 해결 방법은 동적 브로드캐스트 수신자를 사용하는 것이다.
- 이 경우 수신자를 등록할 때는 Context.registerReceiver(BroadcastReceiver, IntentFilter)를 호출하고,
해제할 떄는 Context.unregisterReceiver(BroadcastReceiver)를 호출한다.
- 일반적으로 수신자 자신은 버튼 클릭 리스너처럼 내부 클래스나 람다로 정의된다.
- VisibleFragment를 생성하고 리시버를 생성, 해제하는 코드를 작성한다.
- 그리고 동적 수신자를 받을 Fragment에 입힌다. PhotoFragment : VisibleFragment()
- onCreate에서 생성하면 onDestroy에서 해제하고, onStart에서 생성하면 onStop에서 해제한다.


순차 브로드캐스트 인텐트로 데이터 주고받기
- 마지막으로 동적으로 등록되는 수신자가 다른 수신자보다 항상 먼저 브로드캐스트 인텐트를 수신하고 변경해서 알림이 게시되지 않도록 해야한다.
- private 브로드캐스트 인텐트를 전송할 것인데, 지금까지는 단방향으로 소통하는 브로드캐스트 인텐트를 사용하였다.
PollWorker ->      수신자 #1         ->             수신자 #2
          onReceive(Context, Intent)    onReceive(Context, Intent)

- 이렇게 되는 이유는 일반 브로드캐스트 인텐트를 모든 수신자가 동시에 수신하기 떄문이다.
- 실제로는 onReceive()가 main 스레드에서 호출되므로 수신자들이 동시에 실행되지는 않는다.
- 그렇다고 수신자들이 어떤 특정 순서로 실행되게 하는 것은 불가능하다. 게다가 수신자들의 실행이 모두 끝나는 시점도 알 수 없다.
- 따라서 브로드캐스트 수신자들 상호간의 소통은 쉽지 않으며, 인텐트 전송자가 수신자로부터 정보를 얻기도 어렵다.

순차(ordered) 브로드캐스트 인텐트
- 순차 브로드캐스트 인텐트를 사용하면 양방향 소통을 구현할 수 있다.
- 순차 브로드캐스트는 브로드캐스트 수신자들이 브로드캐스트 인텐트를 순서적으로 받고 처리하도록 해준다.
PollWorker      ->      수신자#1
                  onReceive(Context, Intent) ->     수신자#2
                                                onReceive(Context, Intent)
- 수신자 측에서는 이것이 일반 브로드캐스트와 거의 동일하게 보인다.
그러나 수신자들의 체인을 따라 전달되는 인텐트의 변경에 사용되는 함수들을 사용할 수 있다.
- 여기서는 resultCode 속성을 Activity.RESULT_CANCELED로 설정해서 알림을 취소한다.

수신자와 오래 실행되는 태스크
- main 루프에서 허용하는 실행 제한 시간보다 더 오랫동안 실행되는 태스크를 시작시키고자 브로드캐스트 인텐트를 사용하고 싶다면
1. 그런 작업을 하는 코드를 서비스에 넣은 다음, 브로드캐스트 수신자에서 해당 서비스를 시작시키는 방법이다.
- 서비스는 다수의 요청을 큐에 넣고 차례대로 실행하거나 요청을 관리할 수 있다.
- 참고로 이 방법이 일반적으로 권장되는 방법이다.
- 서비스는 더 긴 작업시간을 가질 수 있지만, 너무 오랫동안 실행되면 여전히 중단될 수 있다.
2. BroadcastReceiver.goAsync() 함수를 사용하는 방법이다.
- 이 함수는 나중에 결과를 제공하는데 사용되는 BroadcastReceiver.PendingResult 객체를 반환한다.
- 따라서 이 객체르르 AstncTask에 전달해서 더 오래 실행되는 작업을 수행하고 PendingResult의 함수들을 호출해서 브로드캐스트 인텐트에 응답하면 된다.
- 단, goAsync() 함수를 사용할 댸는 유연성이 떨어진다는 단점이 있다.

로컬 이벤트
- 브로드캐스트 인텐트는 전역적인 형태로 시스템 전체에 걸쳐 정보를 전파한다.
- 앱의 프로세스 내부(로컬)에서만 이벤트 발생을 전파하고 싶다면 이벤트 버스를 사용하면 된다.

이벤트 버스(event bus)
- 이벤트 버스는 공유 버스나 데이터 스트림의 개념으로 작동하며, 애플리케이션의 컴포넌트가 구독할 수 있다.
- 이벤트가 버스에 게시되면 해당 이벤트를 구독하는 컴포넌트가 시작되고, 이 컴포넌트의 콜백 코드도 실행된다.
- greenrobot의 EventBus, Square의 Otto, RxJava subject와 Observable을 사용할 수 있다.

Event bus 사용하기
1. 앱에서 Event bus를 사용하려면 프로젝트 bundle에 아래 의존성을 추가한다.
implementation 'org.greenrobot:eventbus:3.2.0'
2. 이벤트를 나타내는 클래스를 정의한다.
class NewFriendAddedEvent(val friendName : String)
3. 앱에서 이벤트 클래스 인스턴스를 EventBus에 게시한다.
val eventBus : EventBus = EventBus.getDefault()
eventBus.post(NewFriendAddedEvent("Susie Q"))
4. 앱의 다른 곳에서 EventBus의 리스닝을 등록해서 이벤트를 수신한다. 이 때 액티비티나 프래그먼트의 생명주기 함수에서 등록과 해제를 한다.
5. 적합한 이벤트 타입을 인자로 받는 함수를 구현해서 이벤트를 처리하는 방법을 지정한다. 이때 해당 함수에 @Subscribe 어노테이션을 추가한다.
@Subscribe(threadMode = ThreadMode.MAIN)

RxJava 사용하기
- RxJava도 이벤트 브로드캐스팅 메커니즘을 구현하는 데 사용될 수 있다.
- RxJava는 반응형 자바 코드를 작성하기 위한 라이브러리이다.
- 반응형이란 일련의 연속적인 이벤트들으르 처리하기 위해 이벤트의 발생과 구독을 할 수 있게 해주며, 그런 이벤트 시퀀스를 처리하기 위한 도구들을 제공한다.
1. 이벤트의 발행과 구독을 할 수 있는 객체인 Subject를 생성한다.
val eventBus : Subject<Any, Any> = PublishSubject.create<Any>().toSerialized()
2. 다음과 같이 이벤트를 발행한다.
val someNewFriend = "Susie Q"
val event = NewFriendAddedEvent(someNewFriend)
eventBus.onNext(event)
3. 다음과 같이 이벤트를 구독한다.
eventBus.subscribe{ event : Any ->
    if(event is NewFriendAddedEvent){
    val friendName = event.friendName
    }
}
 */