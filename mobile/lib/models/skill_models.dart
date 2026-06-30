class Skill {
  const Skill({
    required this.id,
    required this.slug,
    required this.name,
    required this.description,
    required this.enabled,
  });

  final String id;
  final String slug;
  final String name;
  final String description;
  final bool enabled;

  factory Skill.fromJson(Map<String, dynamic> json) => Skill(
        id: json['id'] as String,
        slug: json['slug'] as String,
        name: json['name'] as String,
        description: json['description'] as String,
        enabled: json['enabled'] as bool? ?? true,
      );
}
