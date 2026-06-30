import 'package:flutter_test/flutter_test.dart';

import 'package:spicyclaw_mobile/main.dart';

void main() {
  testWidgets('SpicyClaw app boots', (WidgetTester tester) async {
    await tester.pumpWidget(const SpicyclawApp());
    expect(find.text('登录 SpicyClaw'), findsOneWidget);
  });
}
