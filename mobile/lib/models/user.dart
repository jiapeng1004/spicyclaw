class User {
  const User({required this.id, required this.username, required this.displayName});

  final String id;
  final String username;
  final String displayName;

  factory User.fromJson(Map<String, dynamic> json) => User(
        id: json['id'] as String,
        username: json['username'] as String,
        displayName: json['displayName'] as String? ?? json['username'] as String,
      );
}
